package com.lrj.his.security.rbac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法级 RBAC 注解。标注后由 {@link RoleCheckAspect} 校验当前用户角色。
 * 例: {@code @RequiresRole({"DOCTOR"})} 仅医生可开医嘱。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {

    /** 需要的角色编码,满足任一即放行(OR 语义)。 */
    String[] value();
}
