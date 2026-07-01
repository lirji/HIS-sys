package com.lrj.his.api.event;

import java.io.Serializable;
import java.util.List;

/**
 * 退费审批结果事件。审批人通过/驳回退费后发出:
 * notify 通知申请人;outpatient 在通过时撤销对应已执行医嘱。
 */
public record RefundReviewedEvent(
        Long refundRequestId,
        Long invoiceId,
        Long encounterId,
        boolean approved,
        String opinion,
        Long applicantUserId,
        List<Long> orderIds) implements Serializable {
}
