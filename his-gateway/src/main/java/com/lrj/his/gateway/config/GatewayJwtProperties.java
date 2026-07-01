package com.lrj.his.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关 JWT 校验配置。secret 须与 his-auth 一致(同源 Nacos 共享配置)。
 */
@ConfigurationProperties(prefix = "his.jwt")
public class GatewayJwtProperties {

    private String secret = "his-platform-dev-secret-change-me-in-prod-please-32b";
    private long ttlSeconds = 7200;
    private String issuer = "his-auth";

    /** 免鉴权放行路径(Ant 风格) */
    private List<String> permitPaths = new ArrayList<>(List.of(
            "/auth/login",
            "/actuator/**"));

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public List<String> getPermitPaths() {
        return permitPaths;
    }

    public void setPermitPaths(List<String> permitPaths) {
        this.permitPaths = permitPaths;
    }
}
