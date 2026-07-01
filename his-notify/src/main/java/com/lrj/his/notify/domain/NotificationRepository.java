package com.lrj.his.notify.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** 收件箱:本人个人消息 + 本人角色待办。roles 为空时仅查个人消息。 */
    @Query("""
            select n from Notification n
            where n.recipientUserId = :uid
               or (n.recipientUserId is null and n.recipientRole in :roles)
            order by n.createdAt desc
            """)
    List<Notification> findInbox(@Param("uid") Long uid, @Param("roles") Collection<String> roles);

    @Query("""
            select n from Notification n
            where n.readFlag = false
              and (n.recipientUserId = :uid
                   or (n.recipientUserId is null and n.recipientRole in :roles))
            order by n.createdAt desc
            """)
    List<Notification> findUnreadInbox(@Param("uid") Long uid, @Param("roles") Collection<String> roles);

    @Query("""
            select count(n) from Notification n
            where n.readFlag = false
              and (n.recipientUserId = :uid
                   or (n.recipientUserId is null and n.recipientRole in :roles))
            """)
    long countUnread(@Param("uid") Long uid, @Param("roles") Collection<String> roles);

    @Modifying
    @Query("""
            update Notification n set n.readFlag = true
            where n.readFlag = false
              and (n.recipientUserId = :uid
                   or (n.recipientUserId is null and n.recipientRole in :roles))
            """)
    int markAllRead(@Param("uid") Long uid, @Param("roles") Collection<String> roles);
}
