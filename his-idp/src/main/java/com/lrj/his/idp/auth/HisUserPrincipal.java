package com.lrj.his.idp.auth;

import com.lrj.his.api.auth.dto.UserIdentityDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 登录成功后的主体,承载 his-auth 回传的本地身份(uid/dept/roles/realName)。
 * token 定制器据此把身份写进 access_token claims,前端可直接读取。
 */
public record HisUserPrincipal(UserIdentityDto identity) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<String> roles = identity.roles();
        return roles == null ? List.of()
                : roles.stream().map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r)).toList();
    }

    @Override
    public String getPassword() {
        return null; // 口令不在 IdP 落地,核验已委托 his-auth
    }

    @Override
    public String getUsername() {
        return identity.username();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
