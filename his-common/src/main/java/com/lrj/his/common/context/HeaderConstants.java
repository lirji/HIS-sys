package com.lrj.his.common.context;

/**
 * 网关解析 JWT 后,把用户身份通过内部 header 透传给下游服务。
 * 下游不再校验签名(信任内网网关),只读 header 装配 UserContext。
 */
public final class HeaderConstants {

    public static final String USER_ID = "X-His-User-Id";
    public static final String USERNAME = "X-His-Username";
    public static final String DEPT_ID = "X-His-Dept-Id";
    public static final String ROLES = "X-His-Roles"; // 逗号分隔

    private HeaderConstants() {
    }
}
