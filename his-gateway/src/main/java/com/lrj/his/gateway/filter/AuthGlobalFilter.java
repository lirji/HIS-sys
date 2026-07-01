package com.lrj.his.gateway.filter;

import com.lrj.his.common.context.HeaderConstants;
import com.lrj.his.common.jwt.JwtTokenService;
import com.lrj.his.gateway.config.GatewayJwtProperties;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 全局鉴权过滤器。校验 JWT,通过则把用户身份写入内部 header 透传给下游;
 * 同时剥离客户端伪造的 X-His-* header,防越权。放行白名单(登录/actuator)。
 *
 * <p>默认(HS256 自签)模式生效;{@code idp} profile 下由 OAuth2 Resource Server +
 * {@link com.lrj.his.gateway.filter.IdpIdentityGlobalFilter} 接管,本过滤器停用。
 */
@Component
@Profile("!idp")
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthGlobalFilter.class);
    private static final AntPathMatcher MATCHER = new AntPathMatcher();
    private static final String BEARER = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final GatewayJwtProperties props;

    public AuthGlobalFilter(JwtTokenService jwtTokenService, GatewayJwtProperties props) {
        this.jwtTokenService = jwtTokenService;
        this.props = props;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPermitted(path)) {
            return chain.filter(stripIdentityHeaders(exchange));
        }

        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith(BEARER)) {
            return unauthorized(exchange, "缺少 Bearer Token");
        }
        try {
            Claims claims = jwtTokenService.parse(auth.substring(BEARER.length()));
            ServerWebExchange mutated = withIdentityHeaders(exchange, claims);
            return chain.filter(mutated);
        } catch (Exception ex) {
            log.debug("JWT 校验失败: {}", ex.getMessage());
            return unauthorized(exchange, "Token 无效或已过期");
        }
    }

    private boolean isPermitted(String path) {
        return props.getPermitPaths().stream().anyMatch(p -> MATCHER.match(p, path));
    }

    /** 把客户端可能伪造的身份 header 清掉(白名单路径也要清,避免绕过) */
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

    private ServerWebExchange withIdentityHeaders(ServerWebExchange exchange, Claims claims) {
        List<String> roles = JwtTokenService.rolesOf(claims);
        Object uid = claims.get("uid");
        Object dept = claims.get("dept");
        ServerHttpRequest req = exchange.getRequest().mutate()
                .headers(h -> {
                    h.set(HeaderConstants.USER_ID, uid == null ? "" : String.valueOf(uid));
                    h.set(HeaderConstants.USERNAME, claims.getSubject());
                    h.set(HeaderConstants.DEPT_ID, dept == null ? "" : String.valueOf(dept));
                    h.set(HeaderConstants.ROLES, String.join(",", roles));
                })
                .build();
        return exchange.mutate().request(req).build();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":1001,\"message\":\"" + msg + "\",\"data\":null}";
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 早于路由转发
    }
}
