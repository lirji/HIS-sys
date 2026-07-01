package com.lrj.his.notify.domain;

import com.lrj.his.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 通知/待办。收件人语义二选一:
 * - recipientUserId 非空:投递给具体用户的个人消息(如审核结果)。
 * - recipientRole 非空、userId 空:角色待办,该角色全员可见(如待审处方)。
 * bizType/bizId 指向业务对象(如 ENCOUNTER/123),便于前端跳转。
 */
@Entity
@Table(name = "notification")
public class Notification extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_role", length = 32)
    private String recipientRole;

    @Column(name = "recipient_user_id")
    private Long recipientUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationType type;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(length = 1000)
    private String content;

    @Column(name = "biz_type", length = 32)
    private String bizType;

    @Column(name = "biz_id")
    private Long bizId;

    @Column(name = "read_flag", nullable = false)
    private boolean readFlag = false;

    protected Notification() {
    }

    /** 角色待办。 */
    public static Notification toRole(String role, NotificationType type, String title,
                                      String content, String bizType, Long bizId) {
        Notification n = base(type, title, content, bizType, bizId);
        n.recipientRole = role;
        return n;
    }

    /** 个人消息。 */
    public static Notification toUser(Long userId, NotificationType type, String title,
                                      String content, String bizType, Long bizId) {
        Notification n = base(type, title, content, bizType, bizId);
        n.recipientUserId = userId;
        return n;
    }

    private static Notification base(NotificationType type, String title, String content,
                                     String bizType, Long bizId) {
        Notification n = new Notification();
        n.type = type;
        n.title = title;
        n.content = content;
        n.bizType = bizType;
        n.bizId = bizId;
        n.readFlag = false;
        return n;
    }

    public void markRead() {
        this.readFlag = true;
    }

    public Long getId() {
        return id;
    }

    public String getRecipientRole() {
        return recipientRole;
    }

    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getBizType() {
        return bizType;
    }

    public Long getBizId() {
        return bizId;
    }

    public boolean isReadFlag() {
        return readFlag;
    }
}
