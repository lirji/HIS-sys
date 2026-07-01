package com.lrj.his.notify.service;

import com.lrj.his.common.context.CurrentUser;
import com.lrj.his.common.exception.BusinessException;
import com.lrj.his.common.exception.ResultCode;
import com.lrj.his.notify.domain.Notification;
import com.lrj.his.notify.domain.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 通知中心读模型服务。事件落地由监听器调 {@link #save};查询/标记已读按当前登录用户
 * 的 userId + 角色集做收件箱过滤。
 */
@Service
public class NotificationService {

    /** roles 为空时的占位,避免 JPQL `in ()` 边界问题。 */
    private static final Set<String> NO_ROLE = Set.of("__NONE__");

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Notification save(Notification notification) {
        return repository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> inbox(CurrentUser user, boolean onlyUnread) {
        return onlyUnread
                ? repository.findUnreadInbox(user.userId(), roles(user))
                : repository.findInbox(user.userId(), roles(user));
    }

    @Transactional(readOnly = true)
    public long unreadCount(CurrentUser user) {
        return repository.countUnread(user.userId(), roles(user));
    }

    @Transactional
    public void markRead(Long id, CurrentUser user) {
        Notification n = repository.findById(id)
                .orElseThrow(() -> BusinessException.of(ResultCode.NOTIFICATION_NOT_FOUND));
        if (!visibleTo(n, user)) {
            throw BusinessException.of(ResultCode.NOTIFICATION_NOT_FOUND);
        }
        n.markRead();
        repository.save(n);
    }

    @Transactional
    public int markAllRead(CurrentUser user) {
        return repository.markAllRead(user.userId(), roles(user));
    }

    private boolean visibleTo(Notification n, CurrentUser user) {
        if (n.getRecipientUserId() != null) {
            return n.getRecipientUserId().equals(user.userId());
        }
        return n.getRecipientRole() != null && user.hasRole(n.getRecipientRole());
    }

    private Collection<String> roles(CurrentUser user) {
        Set<String> roles = user.roles();
        return (roles == null || roles.isEmpty()) ? NO_ROLE : roles;
    }
}
