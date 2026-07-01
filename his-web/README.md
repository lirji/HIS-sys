# his-web — HIS 平台前端

前后端分离 SPA,对接 his-platform 网关(`:9000`)。技术栈:**React 18 + TypeScript + Vite + Ant Design 5**。

## 技术要点

- **统一网关入口**:开发期所有业务前缀(`/auth /mdm /reg /outpatient /billing /emr /fhir /notify`)经 Vite dev proxy 反代到网关 `localhost:9000`,无需后端开 CORS。生产部署用 Nginx 同样反代这些前缀即可,前端零改动。
- **鉴权**:登录拿 HS256 JWT,存 localStorage;axios 请求拦截器注入 `Authorization: Bearer`;响应拦截器统一解包 `Result<T>`,业务码非 0 弹错,401 自动跳登录。
- **RBAC**:菜单与路由按角色(`ADMIN/DOCTOR/NURSE/CASHIER/PHARMACIST`)过滤,与后端 `@RequiresRole` 对齐;越权页回 403。

## 模块 → 页面

| 页面 | 路由 | 角色 | 对接服务 |
|------|------|------|----------|
| 登录 | `/login` | — | his-auth |
| 工作台 | `/` | 全部 | — |
| 患者主索引 | `/patients` | ADMIN/NURSE | his-mdm |
| 排班挂号 | `/registration` | ADMIN/NURSE/CASHIER | his-registration |
| 门诊医生站 | `/outpatient` | DOCTOR | his-outpatient |
| 药师审方 | `/reviews` | PHARMACIST | his-outpatient |
| 门诊收费 | `/billing` | CASHIER/ADMIN | his-billing |
| 退费审批 | `/refunds` | ADMIN | his-billing |
| 病历 / FHIR | `/emr` | DOCTOR/ADMIN | his-emr |
| 通知中心 | `/notifications` | 全部 | his-notify |

## 运行

先按根 `README.md` 起好基础设施 + 7 个后端服务(网关须在 `:9000`),再:

```bash
cd his-web
pnpm install
pnpm dev      # http://localhost:5173
```

构建产物:`pnpm build`(输出 `dist/`),预览:`pnpm preview`。

演示账号:`admin / doctor / nurse / cashier / pharmacist`,口令均 `123456`(登录页可点标签一键填充)。

## 容器化(生产前后端分离)

多阶段 `Dockerfile`:Node 编译出静态产物 → Nginx 托管 SPA + 反代业务前缀到网关。网关地址经 `GATEWAY_URL` 环境变量 + 启动期 `envsubst` 注入,镜像无需重打。

```bash
# 经 docker compose(已配 host.docker.internal → 宿主机网关 9000)
docker compose up -d --build web      # → http://localhost:8088

# 或独立构建运行
docker build -t his-web ./his-web
docker run -d -p 8088:80 -e GATEWAY_URL=http://host.docker.internal:9000 his-web
```

`nginx.conf.template` 要点:
- `location ~ ^/(auth|mdm|…)/.` —— 只代理**带子路径**的请求到网关;裸前缀(`/billing` `/outpatient` `/emr`)留给 SPA,避免与客户端路由冲突。
- `location / { try_files $uri $uri/ /index.html; }` —— SPA 客户端路由回退。
- 构建用 pnpm 经 `packageManager` 字段锁 `pnpm@9.15.9`,与本地 lockfile 一致、可复现。

## 端到端演示主线

患者建档 → 挂号 → 医生接诊开方(含 DRUG 药品)→ 提交 → 药师审方通过 → 收费缴费 → 查看 FHIR 病历 →(可选)发起退费 → 管理员审批 → 通知中心查看各域待办。
