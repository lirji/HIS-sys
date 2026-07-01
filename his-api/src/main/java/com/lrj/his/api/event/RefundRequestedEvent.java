package com.lrj.his.api.event;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退费申请事件。收费员对已缴费账单发起退费申请后发出,notify 据此生成审批人待办。
 */
public record RefundRequestedEvent(
        Long refundRequestId,
        Long invoiceId,
        Long encounterId,
        BigDecimal amount,
        String reason,
        Long applicantUserId,
        String applicantName) implements Serializable {
}
