package com.lrj.his.common.context;

import java.util.Optional;

/**
 * 请求级用户上下文。注意 JDK21 虚拟线程下仍按"每请求一线程"模型,ThreadLocal 安全;
 * 拦截器在 finally 中务必 clear(),避免线程复用串号。
 */
public final class UserContext {

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static Optional<CurrentUser> get() {
        return Optional.ofNullable(HOLDER.get());
    }

    /** 审计/数据权限取当前用户名,无登录态返回 "system"。 */
    public static String currentUsername() {
        CurrentUser u = HOLDER.get();
        return u != null ? u.username() : "system";
    }

    public static Long currentDeptId() {
        CurrentUser u = HOLDER.get();
        return u != null ? u.deptId() : null;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
