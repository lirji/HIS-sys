package com.lrj.his.billing.domain;

import com.lrj.his.common.audit.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 账单(收费聚合根)。由医嘱事件生成,encounter_id 唯一保证幂等(重复事件不重复建单)。
 * 金额用 BigDecimal,缴费是强一致的本地事务。
 */
@Entity
@Table(name = "invoice")
public class Invoice extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "encounter_id", nullable = false, unique = true)
    private Long encounterId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "dept_code", nullable = false, length = 32)
    private String deptCode;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InvoiceStatus status;

    @Column(name = "pay_method", length = 16)
    private String payMethod;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "invoice_id", nullable = false)
    private List<InvoiceItem> items = new ArrayList<>();

    protected Invoice() {
    }

    public static Invoice create(Long encounterId, Long patientId, String deptCode) {
        Invoice inv = new Invoice();
        inv.encounterId = encounterId;
        inv.patientId = patientId;
        inv.deptCode = deptCode;
        inv.status = InvoiceStatus.UNPAID;
        return inv;
    }

    public void addItem(Long orderId, String itemCode, String itemName, int quantity, BigDecimal amount) {
        items.add(InvoiceItem.of(orderId, itemCode, itemName, quantity, amount));
        this.totalAmount = this.totalAmount.add(amount);
    }

    /** 缴费。返回本单覆盖的医嘱ID,用于回发支付事件推进医嘱执行。 */
    public List<Long> pay(String payMethod) {
        if (status == InvoiceStatus.PAID) {
            throw com.lrj.his.common.exception.BusinessException.of(
                    com.lrj.his.common.exception.ResultCode.INVOICE_ALREADY_PAID);
        }
        this.status = InvoiceStatus.PAID;
        this.payMethod = payMethod;
        this.paidAt = Instant.now();
        return orderIds();
    }

    /** 退费:仅已缴费账单可退,置 REFUNDED。退费审批通过后调用。 */
    public void refund() {
        if (status != InvoiceStatus.PAID) {
            throw com.lrj.his.common.exception.BusinessException.of(
                    com.lrj.his.common.exception.ResultCode.REFUND_NOT_ALLOWED);
        }
        this.status = InvoiceStatus.REFUNDED;
        this.refundedAt = Instant.now();
    }

    /** 本单覆盖的全部医嘱ID。 */
    public List<Long> orderIds() {
        return items.stream().map(InvoiceItem::getOrderId).toList();
    }

    public Long getId() {
        return id;
    }

    public Long getEncounterId() {
        return encounterId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getRefundedAt() {
        return refundedAt;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }
}
