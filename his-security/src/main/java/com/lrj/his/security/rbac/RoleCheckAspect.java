package com.lrj.his.security.rbac;

import com.lrj.his.common.context.CurrentUser;
import com.lrj.his.common.context.UserContext;
import com.lrj.his.common.exception.BusinessException;
import com.lrj.his.common.exception.ResultCode;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.JoinPoint;

import java.util.Arrays;

/**
 * RBAC 切面。拦截 {@link RequiresRole},校验 UserContext 中角色,不满足抛 FORBIDDEN。
 */
@Aspect
public class RoleCheckAspect {

    @Before("@annotation(com.lrj.his.security.rbac.RequiresRole) " +
            "|| @within(com.lrj.his.security.rbac.RequiresRole)")
    public void check(JoinPoint joinPoint) {
        RequiresRole required = resolveAnnotation(joinPoint);
        if (required == null) {
            return;
        }
        CurrentUser user = UserContext.get()
                .orElseThrow(() -> BusinessException.of(ResultCode.UNAUTHORIZED));
        boolean allowed = Arrays.stream(required.value()).anyMatch(user::hasRole);
        if (!allowed) {
            throw BusinessException.of(ResultCode.FORBIDDEN,
                    "需要角色之一: " + String.join("/", required.value()));
        }
    }

    private RequiresRole resolveAnnotation(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RequiresRole onMethod = signature.getMethod().getAnnotation(RequiresRole.class);
        if (onMethod != null) {
            return onMethod;
        }
        return joinPoint.getTarget().getClass().getAnnotation(RequiresRole.class);
    }
}
