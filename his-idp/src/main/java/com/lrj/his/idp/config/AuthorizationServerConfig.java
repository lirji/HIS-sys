package com.lrj.his.idp.config;

import com.lrj.his.idp.auth.HisUserPrincipal;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.time.Duration;
import java.util.UUID;

/**
 * 授权服务器配置。暴露 /oauth2/authorize、/oauth2/token、/oauth2/jwks、/.well-known/openid-configuration;
 * his-web 注册为公共客户端(无密钥)走授权码 + PKCE;access_token 为自包含 JWT,claims 携带本地身份。
 */
@Configuration
@EnableConfigurationProperties(IdpProperties.class)
public class AuthorizationServerConfig {

    /** 授权服务器端点安全链(优先级最高),启用 OIDC;未认证的浏览器请求重定向到 /login。 */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());
        http
                // token/jwks 端点由浏览器跨源 fetch(PKCE 换 token),放行前端源
                .cors(Customizer.withDefaults())
                .exceptionHandling(e -> e.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));
        return http.build();
    }

    /** his-web 公共客户端:授权码 + 刷新令牌,强制 PKCE,免用户确认(内部单点)。 */
    @Bean
    public RegisteredClientRepository registeredClientRepository(IdpProperties props) {
        RegisteredClient web = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("his-web")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUris(uris -> uris.addAll(props.getRedirectUris()))
                .postLogoutRedirectUris(uris -> uris.addAll(props.getPostLogoutRedirectUris()))
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                        .accessTokenTimeToLive(Duration.ofSeconds(props.getAccessTokenTtlSeconds()))
                        .reuseRefreshTokens(false)
                        .build())
                .build();
        return new InMemoryRegisteredClientRepository(web);
    }

    /** 把本地身份写入 access_token,便于前端读取(subject 即 username;网关另行回查作权威来源)。 */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            // access_token 供网关/下游;id_token 供前端读取身份(菜单/路由按角色过滤)
            String tokenType = context.getTokenType().getValue();
            boolean isAccess = OAuth2TokenType.ACCESS_TOKEN.getValue().equals(tokenType);
            boolean isId = OidcParameterNames.ID_TOKEN.equals(tokenType);
            if (!isAccess && !isId) {
                return;
            }
            Object principal = context.getPrincipal().getPrincipal();
            if (principal instanceof HisUserPrincipal hup) {
                var id = hup.identity();
                context.getClaims().claims(claims -> {
                    claims.put("preferred_username", id.username());
                    claims.put("uid", id.userId());
                    claims.put("dept", id.deptId());
                    claims.put("roles", id.roles());
                    claims.put("realName", id.realName());
                });
            }
        };
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(IdpProperties props) {
        return AuthorizationServerSettings.builder()
                .issuer(props.getIssuer())
                .build();
    }
}
