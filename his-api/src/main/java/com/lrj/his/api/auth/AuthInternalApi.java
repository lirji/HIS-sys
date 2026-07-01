package com.lrj.his.api.auth;

import com.lrj.his.api.auth.dto.UserIdentityDto;
import com.lrj.his.api.auth.dto.VerifyCredentialsRequest;
import com.lrj.his.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 认证服务 — 内部身份契约(不经网关 RBAC,仅供内网服务间调用)。
 * his-idp 用 {@link #verify} 校验登录口令;网关按 username 回查身份用 {@link #getByUsername}
 * (网关是 WebFlux,实际用 WebClient 直连,不走本 Feign;此契约供 servlet 侧 his-idp 使用)。
 */
@FeignClient(name = "his-auth", path = "/auth/internal", contextId = "authInternalApi")
public interface AuthInternalApi {

    /** 校验用户名/口令,成功返回本地身份,失败抛 UNAUTHORIZED。 */
    @PostMapping("/verify")
    Result<UserIdentityDto> verify(@RequestBody VerifyCredentialsRequest request);

    /** 按用户名取本地身份(uid/dept/roles)。 */
    @GetMapping("/users/{username}")
    Result<UserIdentityDto> getByUsername(@PathVariable("username") String username);
}
