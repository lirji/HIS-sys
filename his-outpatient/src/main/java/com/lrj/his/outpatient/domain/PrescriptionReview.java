package com.lrj.his.outpatient.domain;

import com.lrj.his.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 处方审核留痕。真实 HIS 须保留药师审方记录(谁审的、通过/驳回、意见、时间),
 * 一条记录对应一次就诊的一次审核动作。
 */
@Entity
@Table(name = "prescription_review")
public class PrescriptionReview extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "encounter_id", nullable = false)
    private Long encounterId;

    @Column(name = "pharmacist_id")
    private Long pharmacistId;

    @Column(name = "pharmacist_name", length = 64)
    private String pharmacistName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReviewResult result;

    @Column(length = 500)
    private String opinion;

    @Column(name = "reviewed_at", nullable = false)
    private Instant reviewedAt;

    protected PrescriptionReview() {
    }

    public static PrescriptionReview of(Long encounterId, Long pharmacistId, String pharmacistName,
                                        ReviewResult result, String opinion) {
        PrescriptionReview r = new PrescriptionReview();
        r.encounterId = encounterId;
        r.pharmacistId = pharmacistId;
        r.pharmacistName = pharmacistName;
        r.result = result;
        r.opinion = opinion;
        r.reviewedAt = Instant.now();
        return r;
    }

    public Long getId() {
        return id;
    }

    public Long getEncounterId() {
        return encounterId;
    }

    public Long getPharmacistId() {
        return pharmacistId;
    }

    public String getPharmacistName() {
        return pharmacistName;
    }

    public ReviewResult getResult() {
        return result;
    }

    public String getOpinion() {
        return opinion;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }
}
