package com.lrj.his.common.context;

import java.io.Serializable;
import java.util.Set;

/**
 * 当前登录用户上下文快照。由网关解析 JWT 后下传 header,各服务的拦截器装配到 UserContext。
 *
 * @param userId   用户ID
 * @param username 登录名
 * @param deptId   科室ID — 数据权限隔离用
 * @param roles    角色编码集合 — RBAC 用
 */
public record CurrentUser(Long userId, String username, Long deptId, Set<String> roles) implements Serializable {

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
