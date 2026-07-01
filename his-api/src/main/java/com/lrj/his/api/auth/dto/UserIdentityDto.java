package com.lrj.his.api.auth.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 本地用户身份投影。IdP 只认证凭证并签发 token(subject=username);
 * 网关拿到 token 后按 username 回查本地身份(uid/dept/roles),再透传 X-His-*。
 * his-idp 认证时也复用它取角色。
 */
public record UserIdentityDto(
        Long userId,
        String username,
        String realName,
        Long deptId,
        List<String> roles) implements Serializable {
}
