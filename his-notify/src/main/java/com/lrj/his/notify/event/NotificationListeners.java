package com.lrj.his.notify.event;

import com.lrj.his.api.event.RefundRequestedEvent;
import com.lrj.his.api.event.RefundReviewedEvent;
import com.lrj.his.api.event.RxReviewRequestedEvent;
import com.lrj.his.api.event.RxReviewedEvent;
import com.lrj.his.api.event.Topics;
import com.lrj.his.notify.domain.Notification;
import com.lrj.his.notify.domain.NotificationType;
import com.lrj.his.notify.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 各域事件 → 通知落地。审批待办投给角色,审批结果投给具体人。
 */
@Component
public class NotificationListeners {

    private static final Logger log = LoggerFactory.getLogger(NotificationListeners.class);

    private final NotificationService service;

    public NotificationListeners(NotificationService service) {
        this.service = service;
    }

    @KafkaListener(topics = Topics.RX_REVIEW_REQUESTED, groupId = "his-notify")
    public void onRxReviewRequested(RxReviewRequestedEvent e) {
        log.info("处方待审 → 药师待办 encounterId={}", e.encounterId());
        service.save(Notification.toRole("PHARMACIST", NotificationType.RX_REVIEW_TODO,
                "待审处方",
                String.format("科室 %s 医生 %s 提交 %d 项药品处方待审", e.deptCode(), e.doctorName(), e.drugItemCount()),
                "ENCOUNTER", e.encounterId()));
    }

    @KafkaListener(topics = Topics.RX_REVIEWED, groupId = "his-notify")
    public void onRxReviewed(RxReviewedEvent e) {
        log.info("处方审核结果 → 通知医生 doctorId={} passed={}", e.doctorId(), e.passed());
        String title = e.passed() ? "处方审核通过" : "处方被驳回";
        String content = e.passed()
                ? String.format("就诊 %d 的处方已由 %s 审核通过", e.encounterId(), e.pharmacistName())
                : String.format("就诊 %d 的处方被 %s 驳回:%s", e.encounterId(), e.pharmacistName(), e.opinion());
        service.save(Notification.toUser(e.doctorId(), NotificationType.RX_REVIEW_RESULT,
                title, content, "ENCOUNTER", e.encounterId()));
    }

    @KafkaListener(topics = Topics.REFUND_REQUESTED, groupId = "his-notify")
    public void onRefundRequested(RefundRequestedEvent e) {
        log.info("退费申请 → 审批人待办 refundRequestId={}", e.refundRequestId());
        service.save(Notification.toRole("ADMIN", NotificationType.REFUND_TODO,
                "待审批退费",
                String.format("%s 对账单 %d 发起退费 ¥%s,原因:%s",
                        e.applicantName(), e.invoiceId(), e.amount(), e.reason()),
                "REFUND_REQUEST", e.refundRequestId()));
    }

    @KafkaListener(topics = Topics.REFUND_REVIEWED, groupId = "his-notify")
    public void onRefundReviewed(RefundReviewedEvent e) {
        log.info("退费审批结果 → 通知申请人 applicantUserId={} approved={}", e.applicantUserId(), e.approved());
        String title = e.approved() ? "退费已通过" : "退费被驳回";
        String content = e.approved()
                ? String.format("账单 %d 的退费申请已通过", e.invoiceId())
                : String.format("账单 %d 的退费申请被驳回:%s", e.invoiceId(), e.opinion());
        service.save(Notification.toUser(e.applicantUserId(), NotificationType.REFUND_RESULT,
                title, content, "REFUND_REQUEST", e.refundRequestId()));
    }
}
