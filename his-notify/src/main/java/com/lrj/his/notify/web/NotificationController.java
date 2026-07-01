package com.lrj.his.notify.web;

import com.lrj.his.common.context.CurrentUser;
import com.lrj.his.common.context.UserContext;
import com.lrj.his.common.exception.BusinessException;
import com.lrj.his.common.exception.ResultCode;
import com.lrj.his.common.web.Result;
import com.lrj.his.notify.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 通知/待办收件箱。不限角色——每个登录用户都能看自己的收件箱(按 userId + 角色过滤)。
 * 登录强制由网关 JWT 保证。
 */
@RestController
@RequestMapping("/notify")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping("/inbox")
    public Result<List<NotificationVo>> inbox(
            @RequestParam(value = "onlyUnread", defaultValue = "false") boolean onlyUnread) {
        return Result.ok(service.inbox(currentUser(), onlyUnread).stream()
                .map(NotificationVo::from).toList());
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        return Result.ok(Map.of("count", service.unreadCount(currentUser())));
    }

    @PostMapping("/{id}/read")
    public Result<Void> read(@PathVariable("id") Long id) {
        service.markRead(id, currentUser());
        return Result.ok();
    }

    @PostMapping("/read-all")
    public Result<Map<String, Integer>> readAll() {
        return Result.ok(Map.of("updated", service.markAllRead(currentUser())));
    }

    private CurrentUser currentUser() {
        return UserContext.get()
                .orElseThrow(() -> BusinessException.of(ResultCode.UNAUTHORIZED));
    }
}
