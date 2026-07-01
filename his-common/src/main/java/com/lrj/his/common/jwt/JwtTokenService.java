package com.lrj.his.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * 框架无关的 JWT 签发/校验 (HS256 共享密钥)。
 * his-auth 用它签发,his-gateway 用它校验 — 同密钥即可。
 * 密钥来自配置 (dev: application.yml; prod: Nacos 共享配置),长度需 ≥32 字节。
 */
public class JwtTokenService {

    private final SecretKey key;
    private final long ttlSeconds;
    private final String issuer;

    public JwtTokenService(String secret, long ttlSeconds, String issuer) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlSeconds = ttlSeconds;
        this.issuer = issuer;
    }

    /** 签发 token。subject=username,自定义 claim 携带 userId/deptId/roles。 */
    public String issue(Long userId, String username, Long deptId, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(username)
                .claim("uid", userId)
                .claim("dept", deptId)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key)
                .compact();
    }

    /** 校验签名与有效期并解析 claims;非法/过期抛 JwtException。 */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @SuppressWarnings("unchecked")
    public static List<String> rolesOf(Claims claims) {
        Object roles = claims.get("roles");
        return roles instanceof List<?> list ? (List<String>) list : List.of();
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }
}
