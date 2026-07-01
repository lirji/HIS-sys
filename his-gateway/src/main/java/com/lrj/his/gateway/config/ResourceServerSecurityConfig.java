package com.lrj.his.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * {@code idp} profile:网关作为 OAuth2 Resource Server,经 IdP 的 JWK(RS256)校验 access_token。
 * 校验通过后由 {@link com.lrj.his.gateway.filter.IdpIdentityGlobalFilter} 回查 his-auth 换本地身份、
 * 透传 X-His-*。actuator 放行,其余需认证。
 */
@Configuration
@Profile("idp")
public class ResourceServerSecurityConfig {

    @Bean
    public SecurityWebFilterChain resourceServerSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers("/actuator/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }
}
