package com.lrj.his.auth.config;

import com.lrj.his.auth.domain.SysRole;
import com.lrj.his.auth.domain.SysUser;
import com.lrj.his.auth.domain.SysUserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 首启播种默认账号(口令统一 123456,BCrypt 编码)。表为空时执行,幂等。
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String DEFAULT_PWD = "123456";

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager em;

    public DataInitializer(SysUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        String hash = passwordEncoder.encode(DEFAULT_PWD);
        createUser("admin", "管理员", null, hash, "ADMIN");
        createUser("doctor", "张医生", 101L, hash, "DOCTOR");
        createUser("nurse", "李护士", 101L, hash, "NURSE");
        createUser("cashier", "王收费", 201L, hash, "CASHIER");
        createUser("pharmacist", "赵药师", 301L, hash, "PHARMACIST");
        log.info("已播种默认账号 admin/doctor/nurse/cashier/pharmacist,口令 {}", DEFAULT_PWD);
    }

    private void createUser(String username, String realName, Long deptId, String hash, String roleCode) {
        SysRole role = em.createQuery("select r from SysRole r where r.code = :c", SysRole.class)
                .setParameter("c", roleCode)
                .getSingleResult();
        SysUser user = SysUser.create(username, hash, realName, deptId);
        user.grant(role);
        userRepository.save(user);
    }
}
