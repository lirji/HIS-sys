package com.lrj.his.billing.domain;

import com.lrj.his.common.audit.AuditableEntity;
import com.lrj.his.common.exception.BusinessException;
import com.lrj.his.common.exception.ResultCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 退费申请(退费审批流聚合根)。收费员发起,审批人(ADMIN)通过/驳回。
 * 状态流转校验内聚在充血方法中,非待审态再次审批抛 REFUND_NOT_PENDING。
 */
@Entity
@Table(name = "refund_request")
public class RefundRequest extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "encounter_id", nullable = false)
    private Long encounterId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RefundStatus status;

    @Column(length = 500)
    private String opinion;

    @Column(name = "applicant_user_id")
    private Long applicantUserId;

    @Column(name = "applicant_name", length = 64)
    private String applicantName;

    @Column(name = "reviewer_name", length = 64)
    private String reviewerName;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    protected RefundRequest() {
    }

    public static RefundRequest open(Long invoiceId, Long encounterId, BigDecimal amount,
                                     String reason, Long applicantUserId, String applicantName) {
        RefundRequest r = new RefundRequest();
        r.invoiceId = invoiceId;
        r.encounterId = encounterId;
        r.amount = amount;
        r.reason = reason;
        r.applicantUserId = applicantUserId;
        r.applicantName = applicantName;
        r.status = RefundStatus.REQUESTED;
        return r;
    }

    public void approve(String reviewerName) {
        ensurePending();
        this.status = RefundStatus.APPROVED;
        this.reviewerName = reviewerName;
        this.reviewedAt = Instant.now();
    }

    public void reject(String reviewerName, String opinion) {
        ensurePending();
        this.status = RefundStatus.REJECTED;
        this.reviewerName = reviewerName;
        this.opinion = opinion;
        this.reviewedAt = Instant.now();
    }

    private void ensurePending() {
        if (status != RefundStatus.REQUESTED) {
            throw BusinessException.of(ResultCode.REFUND_NOT_PENDING);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public Long getEncounterId() {
        return encounterId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public String getOpinion() {
        return opinion;
    }

    public Long getApplicantUserId() {
        return applicantUserId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }
}
