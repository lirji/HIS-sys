package com.lrj.his.auth.domain;

import com.lrj.his.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统用户(医护/管理员)。承载登录凭证 + 科室归属 + 角色,是签发 JWT 的数据源。
 */
@Entity
@Table(name = "sys_user")
public class SysUser extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    /** BCrypt 散列后的口令 */
    @Column(nullable = false, length = 100)
    private String password;

    @Column(name = "real_name", nullable = false, length = 64)
    private String realName;

    /** 科室ID — 下传到下游做数据权限隔离 */
    @Column(name = "dept_id")
    private Long deptId;

    @Column(nullable = false)
    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "sys_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<SysRole> roles = new HashSet<>();

    protected SysUser() {
    }

    /** 工厂:创建启用状态的用户。口令须为已编码散列。 */
    public static SysUser create(String username, String encodedPassword, String realName, Long deptId) {
        SysUser u = new SysUser();
        u.username = username;
        u.password = encodedPassword;
        u.realName = realName;
        u.deptId = deptId;
        u.enabled = true;
        return u;
    }

    public void grant(SysRole role) {
        this.roles.add(role);
    }

    public Set<String> roleCodes() {
        return roles.stream().map(SysRole::getCode).collect(Collectors.toSet());
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRealName() {
        return realName;
    }

    public Long getDeptId() {
        return deptId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Set<SysRole> getRoles() {
        return roles;
    }
}
