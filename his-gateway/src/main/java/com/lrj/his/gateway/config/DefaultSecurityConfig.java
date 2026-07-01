package com.lrj.his.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * 默认(HS256 自签)模式的 WebFlux 安全链。引入 oauth2-resource-server 后 Spring Security
 * 会默认拦截全部请求,这里显式放行 —— 鉴权仍由 {@link com.lrj.his.gateway.filter.AuthGlobalFilter}
 * 手工完成。仅在非 {@code idp} profile 生效。
 */
@Configuration
@Profile("!idp")
public class DefaultSecurityConfig {

    @Bean
    public SecurityWebFilterChain defaultSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(ex -> ex.anyExchange().permitAll())
                .build();
    }
}
