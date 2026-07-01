package com.lrj.his.security.web;

import com.lrj.his.common.context.CurrentUser;
import com.lrj.his.common.context.HeaderConstants;
import com.lrj.his.common.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 读取网关透传的 X-His-* header,装配 {@link UserContext}。
 * 下游服务信任内网网关,不再校验 JWT 签名。afterCompletion 必须清理 ThreadLocal。
 */
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(HeaderConstants.USER_ID);
        if (StringUtils.hasText(userId)) {
            Set<String> roles = parseRoles(request.getHeader(HeaderConstants.ROLES));
            Long deptId = parseLong(request.getHeader(HeaderConstants.DEPT_ID));
            UserContext.set(new CurrentUser(
                    Long.valueOf(userId),
                    request.getHeader(HeaderConstants.USERNAME),
                    deptId,
                    roles));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private static Set<String> parseRoles(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Set.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    private static Long parseLong(String raw) {
        return StringUtils.hasText(raw) ? Long.valueOf(raw) : null;
    }
}
