# his-platform — 医疗信息系统 (HIS + EMR) 微服务平台

作品集项目:用 Spring Boot 3.3 / Spring Cloud 2023 / JDK21 展示 DDD 领域建模、微服务治理、FHIR R4 医疗标准、医嘱状态机、事件驱动一致性、审计与数据权限。

## 技术基线

Java 21(虚拟线程)/ Spring Boot 3.3.5 / Spring Cloud 2023.0.3 / Spring Cloud Alibaba 2023.0.1.3(Nacos + Sentinel)。
持久层 Spring Data JPA + Flyway,数据库 PostgreSQL(DB per service),缓存/锁 Redis,消息 Kafka,病历标准 HAPI FHIR,医嘱状态机 Spring StateMachine。构建用仓库自带 `./mvnw`。

## 模块

| 模块 | 端口 | 职责 |
|------|------|------|
| his-common | — | 统一响应/异常/分页、用户上下文、审计基类 |
| his-api | — | 服务间 OpenFeign 契约 + DTO |
| his-security | — | 网关 header → UserContext 装配、RBAC 注解、数据权限 |
| his-gateway | 9000 | 统一入口、JWT 校验、限流 |
| his-auth | 9001 | 认证、RBAC、签发 JWT |
| his-mdm | 9002 | 患者主索引 EMPI、字典 |
| his-registration | 9003 | 排班/号源/挂号(Redis 锁防超挂) |
| his-outpatient | 9004 | 就诊、医嘱(StateMachine + Kafka 事件) |
| his-billing | 9006 | 收费结算(消费医嘱事件计费)、退费审批流 |
| his-emr | 9005 | 病历文档、FHIR R4 输出 |
| his-notify | 9007 | 通知/待办中心(消费各域事件落地通知,收件箱/未读数) |
| his-web | 5173 / 8088 | 前后端分离 SPA(React 18 + TS + Vite + Ant Design)。dev 经 Vite proxy、生产经 Nginx 反代业务前缀到网关;覆盖全部域页面 + 按角色过滤菜单/路由 |

## 运行方式

提供两种运行模式,**二选一**(都会占用 9000–9007,不可同时跑)。

### 方式 A:全栈容器化(推荐,一条命令)

infra + 8 后端 + 前端全部跑容器,本机无需装 Maven/JDK/Node:

```bash
docker compose up -d --build
```

起来后:

| 入口 | 地址 |
|------|------|
| 前端(Nginx) | http://localhost:8088 |
| 网关 API | http://localhost:9000 |
| Grafana 看板 | http://localhost:3000 (admin/admin) |
| Prometheus | http://localhost:9090 |
| Nacos 控制台 | http://localhost:8848/nacos |

要点(详见 `Dockerfile` / `docker-compose.yml`):

- **后端镜像**:根 `Dockerfile` 多 target,**共享一次 Maven 全量构建**(BuildKit 缓存 `.m2`,8 服务只编译一次),各服务再 COPY 自己的 jar 进精简 JRE 镜像。
- **容器内连接**:经 `SPRING_*` 环境变量把 Nacos/PG/Redis/Kafka 指向 compose DNS(`nacos:8848` / `postgres:5432` / `redis:6379` / `kafka:19092`),不改 `application.yml`。
- **Kafka 双 listener**:`HOST://localhost:9092`(宿主机 jar 用)+ `INTERNAL://kafka:19092`(容器用),两种模式互不影响。
- **前端容器**:Nginx 托管 SPA + 反代 8 个业务前缀到网关;`GATEWAY_URL` 经 `envsubst` 注入。业务前缀反代要求带子路径(`^/(prefix)/.`),避免与裸 SPA 路由(`/billing` `/outpatient` `/emr`)冲突。

常用运维:

```bash
docker compose ps                 # 看全栈状态
docker compose logs -f outpatient # 跟某服务日志
docker compose up -d --build outpatient   # 改代码后只重建并重启某服务
docker compose down               # 停全部(加 -v 连数据卷一起删)
```

### 方式 B:基础设施容器 + 后端本机 jar(便于断点调试)

```bash
docker compose up -d nacos postgres redis kafka prometheus grafana   # 只起 infra
export JAVA_HOME=/Users/liruijun/Library/Java/JavaVirtualMachines/ms-21.0.11/Contents/Home
./mvnw clean install -DskipTests          # 全量构建
./mvnw -pl his-auth spring-boot:run       # 单服务启动(示例,其余同理)
```

前端单独跑 dev(热更新,经 Vite proxy 直连网关 9000):

```bash
cd his-web && pnpm install && pnpm dev     # http://localhost:5173
```

> 切换模式前先停掉另一边对 9000–9007 的占用:容器模式 `docker compose stop gateway auth mdm registration outpatient emr billing notify`;本机模式杀掉对应 `java -jar` 进程。

### 演示账号

`admin / doctor / nurse / cashier / pharmacist`,口令均 `123456`(前端登录页可点标签一键填充)。

## 落地进度

- [x] Step 0 工程骨架(父 pom + common/api/security + docker-compose)
- [x] Step 1 鉴权 + 网关贯通(his-auth 签发 JWT / his-gateway 校验+透传)
- [x] Step 2 主数据 + 挂号(EMPI 去重 / RBAC / Redis 原子号源锁防超挂)
- [x] Step 3 门诊医嘱 + 计费闭环(充血模型 + StateMachine 医嘱状态机 + Kafka 双向事件驱动 Saga)
- [x] Step 4 EMR + FHIR(HAPI FHIR R4 病历文档 JSONB 落库 + FHIR REST;监听 his.invoice.paid 闭环后自动生成)
- [x] Step 5 治理与可观测(网关 Sentinel 限流 + Prometheus/Grafana 看板自动装配)
- [x] Step 6 审批流 + 通知中心(药师审方审批流 + 退费审批流 + 新建 his-notify 事件驱动通知中心)
- [x] 前端 his-web(React + Ant Design,覆盖全部域页面 + 按角色 RBAC 菜单/路由,Vite/Nginx 反代网关)
- [x] 全栈容器化(根 Dockerfile 多 target 出 8 后端镜像 + Nginx 前端镜像,`docker compose up -d --build` 一键起)

> Step 4 / Step 5 已代码完成且编译通过,尚未真机端到端验证。
> **Step 6 已真机端到端验证**(docker 全栈起 7 服务,22 项断言全过:审方推迟计费、通过/驳回、缴费执行、退费撤销、四类通知按角色/用户过滤与已读)。
> **前端 + 全栈容器化已真机验证**:停宿主 jar → 8 后端容器 up → 经容器化网关跑门诊主线 **24 项断言全过**(覆盖 Nacos 发现路由、Postgres、Redis 号源锁、Kafka 内部 listener 事件链);前端 `:8088 → 容器网关` 登录/SPA 回退/带 token API 均通。

## 审批流与通知(Step 6)

- **药师审方审批流**:医生提交医嘱时,**药品(DRUG)处方**进入 `PENDING_REVIEW` 送药师审核,其余直达 `SUBMITTED`。**计费推迟**到就诊无待审/驳回医嘱时一次性发 `OrderPlaced`(配合 billing 端 encounterId 唯一,绝不重复建单)。药师 `GET /outpatient/reviews/pending` 取待审、`POST /{enc}/pass|/reject` 审核;驳回后医生可 `resubmit` 重提或 `cancel` 作废。审方留痕入 `prescription_review`。
- **退费审批流**:收费员对已缴费账单 `POST /billing/invoices/{id}/refund-requests` 发起 → 审批人(ADMIN)`GET /billing/refund-requests/pending`、`approve|reject`。通过后账单置 `REFUNDED` 并发事件,outpatient 撤销对应已执行医嘱。
- **通知/待办中心(his-notify)**:Kafka 消费 `rx.review.requested/reviewed`、`refund.requested/reviewed` 落地通知。角色待办投给角色(药师/审批人),结果通知投给具体人(医生/申请人)。`GET /notify/inbox`、`/notify/unread-count`、`POST /notify/{id}/read`、`/notify/read-all`,按当前登录用户 userId + 角色过滤。
- **默认账号**:新增 `pharmacist`(药师,口令 `123456`),与 admin/doctor/nurse/cashier 并列。

## 治理与可观测

- **限流**:`his-gateway` 集成 Sentinel,`SentinelGatewayConfig` 启动期按路由 id 加载流控规则(`his-auth` 10 QPS、`his-registration` 20 QPS),超限返回统一 `Result` JSON + HTTP 429。无需外部 Sentinel 控制台。
- **指标**:各服务 `actuator` 暴露 `/actuator/prometheus`,Prometheus(`scripts/prometheus.yml`)静态抓取 7 个端口。
- **看板**:Grafana(`http://localhost:3000`,admin/admin)经 `scripts/grafana/provisioning` 自动装配 Prometheus 数据源 + **HIS 总览**看板(服务在线 / HTTP 速率 / P95 延迟 / JVM 堆 / 进程 CPU / 5xx)。
- **审计**:实体继承 `AuditableEntity`,自动记录 who/when 并提供 `@Version` 乐观锁。

项目导航见 `CLAUDE.md`,详细方案见 `~/.claude/plans/idempotent-napping-papert.md`。
