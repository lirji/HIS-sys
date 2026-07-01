package com.lrj.his.auth.service;

import com.lrj.his.api.auth.dto.UserIdentityDto;
import com.lrj.his.auth.domain.SysUser;
import com.lrj.his.auth.domain.SysUserRepository;
import com.lrj.his.auth.web.LoginRequest;
import com.lrj.his.auth.web.LoginResponse;
import com.lrj.his.common.exception.BusinessException;
import com.lrj.his.common.exception.ResultCode;
import com.lrj.his.common.jwt.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(SysUserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        SysUser user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> BusinessException.of(ResultCode.UNAUTHORIZED, "用户名或密码错误"));
        if (!user.isEnabled()) {
            throw BusinessException.of(ResultCode.FORBIDDEN, "账号已停用");
        }
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        List<String> roles = List.copyOf(user.roleCodes());
        String token = jwtTokenService.issue(user.getId(), user.getUsername(), user.getDeptId(), roles);
        return new LoginResponse(
                token,
                jwtTokenService.getTtlSeconds(),
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getDeptId(),
                user.roleCodes());
    }

    /**
     * 校验用户名/口令(供 his-idp 授权码登录调用)。成功返回本地身份,失败抛 UNAUTHORIZED。
     * 用户/口令仍以 his-auth 为单一事实来源,IdP 不再单独存口令。
     */
    @Transactional(readOnly = true)
    public UserIdentityDto verify(String username, String rawPassword) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.of(ResultCode.UNAUTHORIZED, "用户名或密码错误"));
        if (!user.isEnabled()) {
            throw BusinessException.of(ResultCode.FORBIDDEN, "账号已停用");
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw BusinessException.of(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        return toIdentity(user);
    }

    /** 按用户名取本地身份(供网关拿 token 后回查 uid/dept/roles 透传 X-His-*)。 */
    @Transactional(readOnly = true)
    public UserIdentityDto loadByUsername(String username) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.of(ResultCode.UNAUTHORIZED, "用户名或密码错误"));
        if (!user.isEnabled()) {
            throw BusinessException.of(ResultCode.FORBIDDEN, "账号已停用");
        }
        return toIdentity(user);
    }

    private UserIdentityDto toIdentity(SysUser user) {
        return new UserIdentityDto(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getDeptId(),
                List.copyOf(user.roleCodes()));
    }
}
