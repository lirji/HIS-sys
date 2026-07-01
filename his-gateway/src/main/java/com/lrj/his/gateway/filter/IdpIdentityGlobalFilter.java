package com.lrj.his.gateway.filter;

import com.lrj.his.api.auth.dto.UserIdentityDto;
import com.lrj.his.common.context.HeaderConstants;
import com.lrj.his.common.web.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
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
 * {@code idp} profile 身份透传过滤器。Resource Server 校验通过后,从 access_token 取 username,
 * 回查 his-auth 换本地 uid/dept/roles,写入 X-His-* 透传下游;并剥离客户端伪造的身份 header。
 *
 * <p>回查结果带 TTL 短缓存,避免每请求一次 Feign;缓存以 username 为键。
 */
@Component
@Profile("idp")
public class IdpIdentityGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(IdpIdentityGlobalFilter.class);
    private static final ParameterizedTypeReference<Result<UserIdentityDto>> IDENTITY_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final long CACHE_TTL_MS = 60_000;

    private final WebClient authClient;
    private final ConcurrentHashMap<String, Cached> cache = new ConcurrentHashMap<>();

    public IdpIdentityGlobalFilter(WebClient.Builder loadBalancedWebClientBuilder) {
        this.authClient = loadBalancedWebClientBuilder.baseUrl("http://his-auth").build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(Jwt.class::isInstance)
                .cast(Jwt.class)
                .flatMap(jwt -> resolveIdentity(usernameOf(jwt))
                        .flatMap(identity -> chain.filter(withIdentityHeaders(exchange, identity))))
                // 无认证态(如放行的 actuator):仅剥离伪造 header 后放行
                .switchIfEmpty(Mono.defer(() -> chain.filter(stripIdentityHeaders(exchange))));
    }

    private static String usernameOf(Jwt jwt) {
        String preferred = jwt.getClaimAsString("preferred_username");
        return preferred != null ? preferred : jwt.getSubject();
    }

    private Mono<UserIdentityDto> resolveIdentity(String username) {
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
        ServerHttpRequest req = exchange.getRequest().mutate()
                .headers(h -> {
                    h.set(HeaderConstants.USER_ID, id.userId() == null ? "" : String.valueOf(id.userId()));
                    h.set(HeaderConstants.USERNAME, id.username() == null ? "" : id.username());
                    h.set(HeaderConstants.DEPT_ID, id.deptId() == null ? "" : String.valueOf(id.deptId()));
                    h.set(HeaderConstants.ROLES, String.join(",", roles));
                })
                .build();
        return exchange.mutate().request(req).build();
    }

    private ServerWebExchange stripIdentityHeaders(ServerWebExchange exchange) {
        ServerHttpRequest req = exchange.getRequest().mutate()
                .headers(h -> {
                    h.remove(HeaderConstants.USER_ID);
                    h.remove(HeaderConstants.USERNAME);
                    h.remove(HeaderConstants.DEPT_ID);
                    h.remove(HeaderConstants.ROLES);
                })
                .build();
        return exchange.mutate().request(req).build();
    }

    @Override
    public int getOrder() {
        return -100; // 早于路由转发
    }

    private record Cached(UserIdentityDto identity, long expireAt) {
    }
}
