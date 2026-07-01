package com.lrj.his.gateway.filter;

import com.lrj.his.api.auth.dto.UserIdentityDto;
import com.lrj.his.common.context.HeaderConstants;
import com.lrj.his.common.web.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code dual} profile 身份透传过滤器。校验通过后按 token 的 issuer 分流:
 * <ul>
 *   <li>his-idp(RS256)—— token 只带 username,回查 his-auth 换本地 uid/dept/roles;</li>
 *   <li>his-auth(HS256)—— uid/dept/roles 本就在 token claims 里,直接读取,无需回查。</li>
 * </ul>
 * 两条都写入 X-His-* 透传下游(下游零改动),并剥离客户端伪造的身份 header。
 */
@Component
@Profile("dual")
public class DualIdentityGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(DualIdentityGlobalFilter.class);
    private static final ParameterizedTypeReference<Result<UserIdentityDto>> IDENTITY_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final long CACHE_TTL_MS = 60_000;

    private final WebClient authClient;
    private final String idpIssuer;
    private final ConcurrentHashMap<String, Cached> cache = new ConcurrentHashMap<>();

    public DualIdentityGlobalFilter(WebClient.Builder loadBalancedWebClientBuilder,
                                    @Value("${his.idp.issuer:http://localhost:9008}") String idpIssuer) {
        this.authClient = loadBalancedWebClientBuilder.baseUrl("http://his-auth").build();
        this.idpIssuer = idpIssuer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(Jwt.class::isInstance)
                .cast(Jwt.class)
                .flatMap(jwt -> resolve(jwt)
                        .flatMap(identity -> chain.filter(withIdentityHeaders(exchange, identity))))
                .switchIfEmpty(Mono.defer(() -> chain.filter(stripIdentityHeaders(exchange))));
    }

    /** 按 issuer 分流取本地身份。用 getClaimAsString 取原始 iss(his-auth 的 iss 非 URL,
     *  {@code jwt.getIssuer()} 会因转 URL 失败抛异常)。 */
    private Mono<UserIdentityDto> resolve(Jwt jwt) {
        String issuer = jwt.getClaimAsString("iss");
        if (idpIssuer.equals(issuer)) {
            return resolveFromAuth(usernameOf(jwt)); // SSO/RS256:回查
        }
        return Mono.just(fromClaims(jwt));            // 本地/HS256:直接读 claims
    }

    private static String usernameOf(Jwt jwt) {
        String preferred = jwt.getClaimAsString("preferred_username");
        return preferred != null ? preferred : jwt.getSubject();
    }

    /** HS256 本地 token:uid/dept/roles 已在 claims 中。 */
    private UserIdentityDto fromClaims(Jwt jwt) {
        return new UserIdentityDto(
                toLong(jwt.getClaim("uid")),
                jwt.getSubject(),
                null,
                toLong(jwt.getClaim("dept")),
                jwt.getClaimAsStringList("roles"));
    }

    private Mono<UserIdentityDto> resolveFromAuth(String username) {
        Cached hit = cache.get(username);
        if (hit != null && hit.expireAt > System.currentTimeMillis()) {
            return Mono.just(hit.identity);
        }
        return authClient.get()
                .uri("/auth/internal/users/{username}", username)
                .retrieve()
                .bodyToMono(IDENTITY_TYPE)
                .flatMap(result -> {
                    if (result == null || !result.success() || result.data() == null) {
                        return Mono.<UserIdentityDto>error(new IllegalStateException("身份回查失败"));
                    }
                    cache.put(username, new Cached(result.data(), System.currentTimeMillis() + CACHE_TTL_MS));
                    return Mono.just(result.data());
                })
                .onErrorResume(ex -> {
                    log.warn("网关回查 his-auth 身份失败 username={}: {}", username, ex.getMessage());
                    return Mono.error(ex);
                });
    }

    private ServerWebExchange withIdentityHeaders(ServerWebExchange exchange, UserIdentityDto id) {
        List<String> roles = id.roles() == null ? List.of() : id.roles();
        HttpHeaders headers = copyHeaders(exchange);
        headers.set(HeaderConstants.USER_ID, id.userId() == null ? "" : String.valueOf(id.userId()));
        headers.set(HeaderConstants.USERNAME, id.username() == null ? "" : id.username());
        headers.set(HeaderConstants.DEPT_ID, id.deptId() == null ? "" : String.valueOf(id.deptId()));
        headers.set(HeaderConstants.ROLES, String.join(",", roles));
        return decorate(exchange, headers);
    }

    private ServerWebExchange stripIdentityHeaders(ServerWebExchange exchange) {
        HttpHeaders headers = copyHeaders(exchange);
        headers.remove(HeaderConstants.USER_ID);
        headers.remove(HeaderConstants.USERNAME);
        headers.remove(HeaderConstants.DEPT_ID);
        headers.remove(HeaderConstants.ROLES);
        return decorate(exchange, headers);
    }

    // 请求头在 Security 链下是只读的,复制到可写 HttpHeaders 再经 decorator 覆盖 getHeaders()。
    private HttpHeaders copyHeaders(ServerWebExchange exchange) {
        HttpHeaders headers = new HttpHeaders();
        headers.addAll(exchange.getRequest().getHeaders());
        return headers;
    }

    private ServerWebExchange decorate(ServerWebExchange exchange, HttpHeaders headers) {
        ServerHttpRequest decorated = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        };
        return exchange.mutate().request(decorated).build();
    }

    private static Long toLong(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.valueOf(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private record Cached(UserIdentityDto identity, long expireAt) {
    }
}
