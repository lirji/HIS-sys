package com.lrj.his.notify.web;

import com.lrj.his.notify.domain.Notification;

import java.time.Instant;

/** 通知视图。 */
public record NotificationVo(
        Long id,
        String type,
        String title,
        String content,
        String bizType,
        Long bizId,
        boolean read,
        Instant createdAt) {

    public static NotificationVo from(Notification n) {
        return new NotificationVo(n.getId(), n.getType().name(), n.getTitle(), n.getContent(),
                n.getBizType(), n.getBizId(), n.isReadFlag(), n.getCreatedAt());
    }
}
