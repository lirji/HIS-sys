package com.lrj.his.auth.web;

import com.lrj.his.api.auth.dto.UserIdentityDto;
import com.lrj.his.api.auth.dto.VerifyCredentialsRequest;
import com.lrj.his.auth.service.AuthService;
import com.lrj.his.common.web.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部身份端点(不经网关 RBAC,仅内网服务间调用)。
 * his-idp 授权码登录用 /verify 核验口令;网关拿 IdP token 后用 /users/{username} 回查身份。
 * 与 {@link com.lrj.his.api.auth.AuthInternalApi} 契约对应。
 */
@RestController
@RequestMapping("/auth/internal")
public class AuthInternalController {

    private final AuthService authService;

    public AuthInternalController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/verify")
    public Result<UserIdentityDto> verify(@RequestBody VerifyCredentialsRequest request) {
        return Result.ok(authService.verify(request.username(), request.password()));
    }

    @GetMapping("/users/{username}")
    public Result<UserIdentityDto> getByUsername(@PathVariable("username") String username) {
        return Result.ok(authService.loadByUsername(username));
    }
}
