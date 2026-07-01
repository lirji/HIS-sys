# syntax=docker/dockerfile:1
# 后端 8 服务统一镜像:共享一次 Maven 全量构建,再按 target 分流出各服务运行镜像。
# 用法(compose 里各服务 build.target 指定):docker compose build auth / outpatient / ...
# BuildKit 会缓存共享的 build 阶段,8 个服务只跑一次 reactor 构建。

# ---- 共享构建阶段:全量打包出所有 jar ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /src
COPY . .
# 缓存挂载 ~/.m2,重复构建免重下依赖(HAPI FHIR 等较大);
# maven.test.skip 连测试编译一并跳过;wagon 重试/超时抵御 Central 偶发断流。
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -q -B -Dmaven.test.skip=true \
      -Dmaven.wagon.http.retryHandler.count=5 \
      -Dmaven.wagon.rto=120000 \
      clean package

# ---- 运行基底:精简 JRE + busybox wget(供 compose healthcheck) ----
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
# 容器内 JVM 友好默认值
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

# ---- 各服务运行镜像:仅 COPY 对应 jar,体积最小 ----
FROM runtime AS gateway
COPY --from=build /src/his-gateway/target/his-gateway-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9000

FROM runtime AS auth
COPY --from=build /src/his-auth/target/his-auth-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9001

FROM runtime AS idp
COPY --from=build /src/his-idp/target/his-idp-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9008

FROM runtime AS mdm
COPY --from=build /src/his-mdm/target/his-mdm-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9002

FROM runtime AS registration
COPY --from=build /src/his-registration/target/his-registration-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9003

FROM runtime AS outpatient
COPY --from=build /src/his-outpatient/target/his-outpatient-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9004

FROM runtime AS emr
COPY --from=build /src/his-emr/target/his-emr-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9005

FROM runtime AS billing
COPY --from=build /src/his-billing/target/his-billing-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9006

FROM runtime AS notify
COPY --from=build /src/his-notify/target/his-notify-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9007
