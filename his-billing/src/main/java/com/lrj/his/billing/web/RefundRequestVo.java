package com.lrj.his.billing.web;

import com.lrj.his.billing.domain.RefundRequest;

import java.math.BigDecimal;
import java.time.Instant;

/** 退费申请视图。 */
public record RefundRequestVo(
        Long id,
        Long invoiceId,
        Long encounterId,
        BigDecimal amount,
        String reason,
        String status,
        String opinion,
        Long applicantUserId,
        String applicantName,
        String reviewerName,
        Instant reviewedAt) {

    public static RefundRequestVo from(RefundRequest r) {
        return new RefundRequestVo(r.getId(), r.getInvoiceId(), r.getEncounterId(), r.getAmount(),
                r.getReason(), r.getStatus().name(), r.getOpinion(), r.getApplicantUserId(),
                r.getApplicantName(), r.getReviewerName(), r.getReviewedAt());
    }
}
