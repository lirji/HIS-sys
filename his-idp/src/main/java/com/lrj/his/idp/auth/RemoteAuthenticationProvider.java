package com.lrj.his.idp.auth;

import com.lrj.his.api.auth.AuthInternalApi;
import com.lrj.his.api.auth.dto.UserIdentityDto;
import com.lrj.his.api.auth.dto.VerifyCredentialsRequest;
import com.lrj.his.common.web.Result;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * 表单登录认证提供者。把用户名/口令经 Feign 委托 his-auth 核验,成功则以 {@link HisUserPrincipal}
 * 为主体返回已认证 token(携带本地 uid/dept/roles)。IdP 不再单独存口令散列。
 */
public class RemoteAuthenticationProvider implements AuthenticationProvider {

    private final AuthInternalApi authApi;

    public RemoteAuthenticationProvider(AuthInternalApi authApi) {
        this.authApi = authApi;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());

        UserIdentityDto identity;
        try {
            Result<UserIdentityDto> result = authApi.verify(new VerifyCredentialsRequest(username, password));
            if (result == null || !result.success() || result.data() == null) {
                throw new BadCredentialsException("用户名或密码错误");
            }
            identity = result.data();
        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            // Feign 4xx(如 his-auth 返回 401)会抛异常,统一归一化为凭证错误
            throw new BadCredentialsException("用户名或密码错误", e);
        }

        HisUserPrincipal principal = new HisUserPrincipal(identity);
        return UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
