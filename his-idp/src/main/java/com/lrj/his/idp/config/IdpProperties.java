package com.lrj.his.idp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * IdP 配置。issuer 需为浏览器可达地址(签发的 token 与 OIDC 元数据据此生成绝对 URL);
 * redirectUris 为前端回调地址;webOrigins 为需放行 CORS 的前端源(token/jwks 端点由浏览器 fetch)。
 */
@ConfigurationProperties(prefix = "his.idp")
public class IdpProperties {

    private String issuer = "http://localhost:9008";
    private long accessTokenTtlSeconds = 3600;
    private List<String> redirectUris = new ArrayList<>(List.of(
            "http://localhost:5173/oidc-callback",
            "http://localhost:8088/oidc-callback"));
    private List<String> postLogoutRedirectUris = new ArrayList<>(List.of(
            "http://localhost:5173/login",
            "http://localhost:8088/login"));
    private List<String> webOrigins = new ArrayList<>(List.of(
            "http://localhost:5173",
            "http://localhost:8088"));

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtlSeconds;
    }

    public void setAccessTokenTtlSeconds(long accessTokenTtlSeconds) {
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    public List<String> getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(List<String> redirectUris) {
        this.redirectUris = redirectUris;
    }

    public List<String> getPostLogoutRedirectUris() {
        return postLogoutRedirectUris;
    }

    public void setPostLogoutRedirectUris(List<String> postLogoutRedirectUris) {
        this.postLogoutRedirectUris = postLogoutRedirectUris;
    }

    public List<String> getWebOrigins() {
        return webOrigins;
    }

    public void setWebOrigins(List<String> webOrigins) {
        this.webOrigins = webOrigins;
    }
}
