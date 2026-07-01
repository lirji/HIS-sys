package com.lrj.his.common.audit;

import com.lrj.his.common.context.UserContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;

import java.time.Instant;

/**
 * 审计基类。所有持久化实体继承,自动填充 who/when。
 * 通过 JPA 生命周期回调读取 {@link UserContext},无需 Spring Data auditing 配置,
 * 故 his-common 不强依赖 spring-data。{@code @Version} 提供乐观锁(号源/账单并发关键)。
 */
@MappedSuperclass
public abstract class AuditableEntity {

    @Column(name = "created_by", updatable = false, length = 64)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        String user = UserContext.currentUsername();
        this.createdAt = now;
        this.updatedAt = now;
        this.createdBy = user;
        this.updatedBy = user;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = UserContext.currentUsername();
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}
