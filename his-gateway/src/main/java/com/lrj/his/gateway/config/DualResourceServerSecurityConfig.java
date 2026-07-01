package com.lrj.his.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerReactiveAuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * {@code dual} profile:网关同时接受两种令牌 —— 本地 his-auth 签发的 **HS256**(共享密钥,issuer=his-auth)
 * 与 his-idp 签发的 **RS256**(JWK,issuer=idp)。按 token 的 {@code iss} claim 分派到对应解码器
 * ({@link JwtIssuerReactiveAuthenticationManagerResolver}),实现"账号密码 + SSO"双登录共存。
 * 校验后由 {@link com.lrj.his.gateway.filter.DualIdentityGlobalFilter} 统一透传 X-His-*。
 *
 * <p>放行 {@code /auth/login}(密码登录需免 token 取 HS256)与 {@code /actuator/**}。
 */
@Configuration
@Profile("dual")
public class DualResourceServerSecurityConfig {

    private final String hsSecret;
    private final String hsIssuer;
    private final String idpJwkSetUri;
    private final String idpIssuer;

    public DualResourceServerSecurityConfig(GatewayJwtProperties hs,
                                            @Value("${his.idp.jwk-set-uri:http://localhost:9008/oauth2/jwks}") String idpJwkSetUri,
                                            @Value("${his.idp.issuer:http://localhost:9008}") String idpIssuer) {
        this.hsSecret = hs.getSecret();
        this.hsIssuer = hs.getIssuer();
        this.idpJwkSetUri = idpJwkSetUri;
        this.idpIssuer = idpIssuer;
    }

    @Bean
    public SecurityWebFilterChain dualSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers("/actuator/**", "/auth/login").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.authenticationManagerResolver(issuerResolver()))
                .build();
    }

    /** 按 token 的 iss 选择解码器:his-auth→HS256 共享密钥;idp→RS256 JWK。 */
    private ReactiveAuthenticationManagerResolver<ServerWebExchange> issuerResolver() {
        ReactiveAuthenticationManager hs = new JwtReactiveAuthenticationManager(hsDecoder());
        ReactiveAuthenticationManager rs = new JwtReactiveAuthenticationManager(rs256Decoder());
        Map<String, ReactiveAuthenticationManager> byIssuer = Map.of(hsIssuer, hs, idpIssuer, rs);
        ReactiveAuthenticationManagerResolver<String> byIssuerResolver =
                issuer -> Mono.justOrEmpty(byIssuer.get(issuer));
        return new JwtIssuerReactiveAuthenticationManagerResolver(byIssuerResolver);
    }

    private ReactiveJwtDecoder hsDecoder() {
        byte[] secretBytes = hsSecret.getBytes(StandardCharsets.UTF_8);
        MacAlgorithm alg = macAlgorithmFor(secretBytes);
        // jjwt(his-auth 侧)按密钥长度自动选 HS256/384/512;网关须与之一致,否则验签失败。
        SecretKey key = new SecretKeySpec(secretBytes, "HmacSHA" + alg.getName().substring(2));
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(key)
                .macAlgorithm(alg)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(hsIssuer));
        return decoder;
    }

    /** 复刻 jjwt Keys.hmacShaKeyFor 的选择逻辑:按密钥位长挑 HS512/384/256。 */
    private static MacAlgorithm macAlgorithmFor(byte[] secretBytes) {
        int bits = secretBytes.length * 8;
        if (bits >= 512) {
            return MacAlgorithm.HS512;
        }
        if (bits >= 384) {
            return MacAlgorithm.HS384;
        }
        return MacAlgorithm.HS256;
    }

    private ReactiveJwtDecoder rs256Decoder() {
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(idpJwkSetUri)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(idpIssuer));
        return decoder;
    }
}
