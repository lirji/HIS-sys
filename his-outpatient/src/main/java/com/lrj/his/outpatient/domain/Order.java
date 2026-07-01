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

import java.math.BigDecimal;

/**
 * 医嘱。state 由状态机驱动流转(见 OrderStateMachineService),实体只持有当前状态 +
 * 金额(单价×数量,BigDecimal 精确计费)。CANCEL/STOP 等约束由状态机定义,不在此堆 if。
 */
@Entity
@Table(name = "med_order")
public class Order extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "encounter_id", nullable = false)
    private Long encounterId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 16)
    private OrderType orderType;

    @Column(name = "item_code", nullable = false, length = 64)
    private String itemCode;

    @Column(name = "item_name", nullable = false, length = 128)
    private String itemName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OrderState state;

    protected Order() {
    }

    public static Order create(Long encounterId, Long patientId, OrderType type,
                               String itemCode, String itemName, int quantity, BigDecimal unitPrice) {
        Order o = new Order();
        o.encounterId = encounterId;
        o.patientId = patientId;
        o.orderType = type;
        o.itemCode = itemCode;
        o.itemName = itemName;
        o.quantity = quantity;
        o.unitPrice = unitPrice;
        o.amount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        o.state = OrderState.CREATED;
        return o;
    }

    /** 由状态机服务校验流转合法后调用,落地新状态。 */
    public void applyState(OrderState newState) {
        this.state = newState;
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

    public OrderType getOrderType() {
        return orderType;
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public OrderState getState() {
        return state;
    }
}
