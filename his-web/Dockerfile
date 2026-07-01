# ---- 构建阶段:Node 编译出静态产物 ----
FROM node:22-alpine AS build
WORKDIR /app
RUN corepack enable
# 先装依赖(利用层缓存:仅 lockfile 变化才重装)
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile
COPY . .
# 鉴权模式(password 默认 / idp SSO)在构建期注入 Vite;idp 模式经 docker-compose.idp.yml 传 VITE_AUTH_MODE=idp
ARG VITE_AUTH_MODE=password
ARG VITE_OIDC_AUTHORITY=http://localhost:9008
ARG VITE_OIDC_CLIENT_ID=his-web
ENV VITE_AUTH_MODE=$VITE_AUTH_MODE \
    VITE_OIDC_AUTHORITY=$VITE_OIDC_AUTHORITY \
    VITE_OIDC_CLIENT_ID=$VITE_OIDC_CLIENT_ID
RUN pnpm build

# ---- 运行阶段:Nginx 托管 SPA + 反代网关 ----
FROM nginx:1.27-alpine AS runtime
# 网关地址,运行时经 envsubst 注入到 nginx 配置;默认指向宿主机上的网关(Docker Desktop)
ENV GATEWAY_URL=http://host.docker.internal:9000
COPY --from=build /app/dist /usr/share/nginx/html
# nginx 官方镜像启动时会对 /etc/nginx/templates/*.template 做 envsubst → /etc/nginx/conf.d/
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
EXPOSE 80
# 沿用基础镜像的 entrypoint(自动 envsubst + 启动 nginx)
