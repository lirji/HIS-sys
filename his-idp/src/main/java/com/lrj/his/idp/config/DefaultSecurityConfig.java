package com.lrj.his.idp.config;

import com.lrj.his.api.auth.AuthInternalApi;
import com.lrj.his.idp.auth.RemoteAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 表单登录安全链(次于授权服务器链)。登录页由 Spring Security 自动生成;
 * 认证委托 {@link RemoteAuthenticationProvider} → his-auth 核验。
 */
@Configuration
public class DefaultSecurityConfig {

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
                                                          AuthenticationProvider remoteAuthenticationProvider)
            throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/assets/**", "/favicon.ico", "/error").permitAll()
                        .anyRequest().authenticated())
                .authenticationProvider(remoteAuthenticationProvider)
                .formLogin(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public AuthenticationProvider remoteAuthenticationProvider(AuthInternalApi authApi) {
        return new RemoteAuthenticationProvider(authApi);
    }

    /** token/jwks/userinfo 端点供前端浏览器跨源 fetch,放行配置的前端源。 */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(IdpProperties props) {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(props.getWebOrigins());
        cfg.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/oauth2/**", cfg);
        source.registerCorsConfiguration("/.well-known/**", cfg);
        source.registerCorsConfiguration("/connect/**", cfg);
        source.registerCorsConfiguration("/userinfo", cfg);
        return source;
    }
}
