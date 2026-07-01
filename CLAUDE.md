# CLAUDE.md — his-platform

医疗信息系统(HIS + EMR)微服务**作品集**项目。定位:展示 DDD 领域建模、Spring Cloud 微服务全家桶、FHIR R4 医疗标准、医嘱状态机、事件驱动一致性、治理与可观测。**不**死磕国家互联互通测评 / 真实医保接口 / 住院全流程 / LIS·PACS 对接。

`groupId=com.lrj.his`,多模块 Maven,六步增量交付(每步可独立构建)。

## 构建 / 运行

本机 **未把 Maven 装进 PATH**,且**只有 JDK 21**(无 17)。跑 mvn 前务必 export,并用仓库自带 `./mvnw`:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # 或 .../JavaVirtualMachines/ms-21.0.11/Contents/Home
export PATH="/Users/liruijun/personal/devUtils/apache-maven-3.9.12/bin:$PATH"  # 若直接用 mvn

./mvnw -q compile                       # 全量编译(最快的"能不能过"检查)
./mvnw -pl his-emr -am compile          # 只编某模块 + 其依赖
./mvnw clean install -DskipTests        # 全量构建产物
./mvnw -pl his-auth spring-boot:run     # 单服务启动
```

先起基础设施再起服务:
```bash
docker compose up -d   # Nacos(8848) PG(5433→5432) Redis(6379) Kafka(9092) Prometheus(9090) Grafana(3000)
```

**全容器化模式**(8 后端 + 前端均跑容器,无需本机 jar/mvn):
```bash
docker compose up -d --build   # 起全部:infra + 8 后端 + web(8088)
# 后端镜像:根 Dockerfile 多 target,共享一次 Maven 构建;容器内连 nacos/postgres/redis/kafka(DNS)
# Kafka 双 listener:宿主 jar 用 localhost:9092,容器用 kafka:19092(由 SPRING_KAFKA_BOOTSTRAP_SERVERS 覆盖)
# 注意:容器与本机 jar 抢 9000-9007,二选一(跑容器前先停 jar)
```
DB per service,库由 `scripts/init-databases.sql` 在 PG 首次启动时建好(his_auth/mdm/registration/outpatient/billing/emr)。

## 模块与端口

| 模块 | 端口 | 职责 |
|------|------|------|
| his-common | — | 统一 `Result<T>`/`ResultCode`、`BusinessException`、`UserContext`、审计基类 `AuditableEntity` |
| his-api | — | 服务间 **OpenFeign 契约** + DTO + **Kafka 事件 DTO**(`event/`) |
| his-security | — | header→`UserContext` 装配(`UserContextInterceptor`)、`@RequiresRole` RBAC 切面,**自动装配**(引入即生效) |
| his-gateway | 9000 | 统一入口、JWT 校验、`X-His-*` 透传、**Sentinel 限流**;`idp` profile 下切 **OAuth2 Resource Server**(RS256+JWK) |
| his-auth | 9001 | 账号密码登录、RBAC、签发 HS256 JWT;内部端点 `/auth/internal/{verify,users/{username}}` 供 IdP 核验 / 网关回查 |
| his-idp | 9008 | **身份提供方(可选,SSO)**:Spring Authorization Server,OIDC 授权码+PKCE,签发 RS256;口令核验委托 his-auth |
| his-mdm | 9002 | 患者主索引 EMPI、字典 |
| his-registration | 9003 | 排班/号源/挂号(Redis Lua 原子锁防超挂) |
| his-outpatient | 9004 | 就诊 + 医嘱(充血模型 + Spring StateMachine + Kafka) |
| his-billing | 9006 | 收费结算(消费医嘱事件计费)+ 退费审批流 |
| his-emr | 9005 | 病历文档 + FHIR R4 输出(HAPI FHIR) |
| his-notify | 9007 | 通知/待办中心(消费各域事件落地通知,收件箱/未读/已读) |
| his-web | 5173(dev)/ 8088(容器) | 前后端分离 SPA(React+TS+Vite+AntD)。dev 经 Vite proxy、生产经 Nginx(`Dockerfile`+`nginx.conf.template`,`GATEWAY_URL` 注入)反代 8 个业务前缀到网关 9000,免 CORS。`pnpm dev` 或 `docker compose up -d --build web`。覆盖全部域页面 + 按角色过滤菜单/路由 |

## 关键约定 / 设计决策

- **鉴权**:HS256 共享密钥 JWT(密钥在 Nacos 共享配置 `his-common.yaml`,dev 落 `application.yml`)。网关校验后透传 `X-His-*` header,下游 `UserContextInterceptor` 装配 `UserContext`。下游服务**信任内网网关,不再校验签名**。默认账号 `admin/doctor/nurse/cashier/pharmacist`,口令 `123456`(`pharmacist`=药师,Step 6 新增,由 `DataInitializer` 仅在用户表空时播种,已有库需清库或手工补)。
- **SSO / OIDC(可选,`idp` profile)**:网关切成 **OAuth2 Resource Server**,对接 `his-idp`(Spring Authorization Server)。前端 `his-web` 走 OIDC **授权码 + PKCE**(公共客户端 `his-web`)。网关经 IdP **JWK(RS256)** 验签后,按 token 的 `preferred_username` **回查 `his-auth`**(reactive `WebClient` + lb://his-auth,60s TTL 缓存)换本地 `uid/dept/roles`,再透传 `X-His-*` —— **下游零改动**。用户/口令仍以 `his-auth` 为单一事实来源(IdP 登录经 Feign 委托 `/auth/internal/verify` 核验,不在 IdP 落口令)。两套鉴权**按 profile 共存**:默认(无 profile)= HS256 自签(`AuthGlobalFilter`,已验证主线);`SPRING_PROFILES_ACTIVE=idp` = SSO(`ResourceServerSecurityConfig` + `IdpIdentityGlobalFilter`,`AuthGlobalFilter` 停用)。引入 `spring-security` 后默认 profile 也需 `DefaultSecurityConfig` 显式 permitAll,否则会误拦全部请求。前端同理:`VITE_AUTH_MODE=idp`(默认 `password`)切 SSO 登录页,`AuthBridge` 把 OIDC 态桥接进既有 zustand store。IdP token 定制器把身份写进 **access + id token**,前端读 id_token claims 做菜单/路由过滤。启动 SSO 全栈:`docker compose -f docker-compose.yml -f docker-compose.idp.yml up -d --build`。
- **服务间调用**:OpenFeign,契约统一放 `his-api`(`@FeignClient(name="his-xxx")` 走 Nacos 服务名负载均衡)。调用方 `@EnableFeignClients(basePackages="com.lrj.his.api")`。**解包约定**:`if (result == null || !result.success() || result.data() == null) throw BusinessException.of(REMOTE_CALL_ERROR)`。
- **GET 投影类接口不加 `@RequiresRole`**:供其它服务(尤其 emr 的 Kafka 触发流程,无 X-His header)内部 Feign 取数。写接口才加 RBAC。
- **事件驱动 Saga**:事件 DTO 放 `his-api/event`,主题常量在 `Topics`。`his.order.placed`(outpatient→billing)、`his.invoice.paid`(billing→outpatient 推进医嘱执行 + emr 生成病历)、`his.rx.review.requested`/`his.rx.reviewed`(outpatient→notify,审方待办/结果)、`his.refund.requested`/`his.refund.reviewed`(billing→notify 通知 + billing→outpatient 退费撤销医嘱)。Kafka 用官方 `apache/kafka:3.8.0`(arm64,KRaft,无 Zookeeper);消费端 `spring.json.trusted.packages=com.lrj.his.api.event`。
- **审批流(Step 6)**:① **药师审方**——医嘱状态机增 `PENDING_REVIEW/REJECTED`;`submitOrders` 按 `orderType` 路由(DRUG 送审、其余直达 SUBMITTED),**计费由 `EncounterService.tryBill` 守卫推迟**:仅当就诊不存在 `{CREATED,PENDING_REVIEW,REJECTED}` 医嘱时才发一次 `OrderPlaced`(`encounter.billed` 标志 + billing encounterId 唯一双重防重)。`PrescriptionReviewService` pass/reject + `prescription_review` 留痕。② **退费审批**——`RefundRequest` 充血状态机(REQUESTED→APPROVED/REJECTED),approve 时 `Invoice.refund()` 置 REFUNDED 并发 `RefundReviewed`(orderIds)驱动 outpatient `cancelExecutedOrders`(EXECUTED→CANCELLED,事件 `REFUND`)。
- **通知中心 his-notify**:事件驱动 read-model。`Notification` 收件人二选一——`recipientRole`(角色待办,全员可见)或 `recipientUserId`(个人消息);收件箱按 `userId OR role IN roles` 过滤(JPQL,roles 空用占位防 `in ()`)。RBAC:审方端点 `@RequiresRole(PHARMACIST)`、退费发起 `CASHIER`/审批 `ADMIN`;notify 收件箱不限角色(登录即可,只看自己的)。
- **幂等**:跨服务以 `encounterId` 作幂等键(账单、病历文档均按 encounterId upsert)。
- **审计/乐观锁**:实体继承 `AuditableEntity`(`@PrePersist/@PreUpdate` 填 who/when,`@Version` 乐观锁)。建表 SQL 要带 `created_by/created_at/updated_by/updated_at/version` 列。
- **可观测**:各服务 `actuator` 暴露 `/actuator/prometheus`;`scripts/prometheus.yml` 静态抓 7 个端口(容器内经 `host.docker.internal`);Grafana 经 `scripts/grafana/provisioning` 自动装配数据源 + `dashboards/his-overview.json` 看板。
- **网关限流**:`SentinelGatewayConfig` 启动期编程式 `GatewayRuleManager.loadRules`,按**路由 id** 限流(`GatewayFlowRule` 默认 `resourceMode=ROUTE_ID`);超限经 `GatewayCallbackManager.setBlockHandler` 返回 `Result` JSON + HTTP 429。无外部 Sentinel 控制台。

## 已知坑(改之前先看)

- **JPA `@OneToMany`**:子实体别再单独映射 FK 列(会与父 `@JoinColumn` 重复报错)。
- **`LazyInitializationException`**:返回含集合的聚合,要么 `EAGER`,要么在事务内组装成 VO/DTO 再返回(`open-in-view: false` 已全局关闭)。
- **改服务代码后务必重启对应 jar/进程**:端口竞态会让旧进程继续服务,看不到改动。
- **新增微服务模块**:别忘在父 `pom.xml` `<modules>` 注册(早期各 Step 用注释逐步放开)。
- **Flyway**:`ddl-auto: none`,所有建表走 `db/migration/V*.sql`,不要让 Hibernate 自动建表。
- **SSO(`idp` profile)专属坑**:① IdP `issuer` 必须**浏览器可达**(默认 `http://localhost:9008`),token/OIDC 元数据据此生成绝对 URL;网关验签用**容器内 DNS** `IDP_JWK_SET_URI=http://his-idp:9008/oauth2/jwks`(两地址不同源,别混用)。② 浏览器 PKCE 换 token 是**跨源 fetch**,his-idp 已对 `/oauth2/**` `/.well-known/**` 开 CORS 放行前端源(`his.idp.web-origins`),改前端端口要同步加。③ 网关是 **WebFlux**,回查 his-auth 用 **reactive `WebClient`(`@LoadBalanced`)**,不是 Feign;`AuthInternalApi` Feign 契约只给 servlet 侧的 his-idp 用。④ IdP RSA 密钥**启动即生成**(重启即轮换,已签发 token 失效)——生产应改为固定密钥。

## 进度

Step 0 骨架 / Step 1 鉴权+网关 / Step 2 主数据+挂号 / Step 3 门诊医嘱+计费闭环 — 均 ✅ 端到端验证过。
Step 4 EMR+FHIR / Step 5 治理可观测(Sentinel+Grafana)— ✅ 代码完成 + 全量编译通过,**尚未真机端到端验证**。
Step 6 审批流+通知中心(药师审方 + 退费审批 + his-notify)— ✅ **已真机端到端验证**(全栈 7 服务,审方推迟计费/通过/驳回/缴费/退费撤销/四类通知 22 项断言全过)。
前端 his-web(React+AntD)— ✅ **已真机联调 + 容器化验证**(全栈 8 服务,Vite proxy 主线 24 项断言全过;Nginx 容器 8088 静态/SPA回退/反代登录/带 token API 全通)。**契约坑**:建档 `gender` 必填(`MALE/FEMALE/UNKNOWN`)、排班 `period` 必填(`AM/PM`),都是后端枚举字段,前端表单已对齐。**Nginx 坑**:业务前缀反代要求带子路径(`^/(prefix)/.`),否则裸前缀 `/billing` `/outpatient` `/emr` 会与 SPA 客户端路由冲突被错代理到网关。
SSO / OIDC(his-idp 授权服务器 + 网关 Resource Server + 前端授权码流,`idp` profile 可选叠加)— ✅ **代码完成 + 全量编译/前端 tsc+build 通过,尚未真机端到端验证**。password/HS256 主线不受影响(默认 profile)。
后端 8 服务容器化(根 Dockerfile 多 target + compose)— ✅ **已整栈容器化验证**(停宿主 jar→8 容器 up→经容器网关主线 24 项断言全过)。**坑**:① `-DskipTests` 仍会解析 surefire 插件,镜像构建用 `-Dmaven.test.skip=true` 并加 wagon 重试抗 Central 偶发断流;② Kafka 必须双 listener,否则容器连 `kafka:9092` 拿到 advertised `localhost` 而失败。
六步方案详见 `~/.claude/plans/idempotent-napping-papert.md`,Step 6 方案见 `~/.claude/plans/lazy-gliding-scone.md`,进度细节见 `README.md`。
