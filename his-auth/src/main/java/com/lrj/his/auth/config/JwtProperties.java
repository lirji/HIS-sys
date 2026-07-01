package com.lrj.his.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置。dev 落 application.yml,prod 建议放 Nacos 共享配置 his-common.yaml,
 * 让 auth 与 gateway 读到同一密钥。
 */
@ConfigurationProperties(prefix = "his.jwt")
public class JwtProperties {

    /** HS256 密钥,长度需 ≥32 字节 */
    private String secret = "his-platform-dev-secret-change-me-in-prod-please-32b";
    private long ttlSeconds = 7200;
    private String issuer = "his-auth";

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
}
