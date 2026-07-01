package com.lrj.his.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * 账单明细。一行对应一条医嘱(orderId 反向追溯),缴费后据此推进医嘱执行。
 */
@Entity
@Table(name = "invoice_item")
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // invoice_id 列由父聚合 Invoice 的 @OneToMany @JoinColumn 管理,此处不再单独映射

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "item_code", nullable = false, length = 64)
    private String itemCode;

    @Column(name = "item_name", nullable = false, length = 128)
    private String itemName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    protected InvoiceItem() {
    }

    static InvoiceItem of(Long orderId, String itemCode, String itemName, int quantity, BigDecimal amount) {
        InvoiceItem i = new InvoiceItem();
        i.orderId = orderId;
        i.itemCode = itemCode;
        i.itemName = itemName;
        i.quantity = quantity;
        i.amount = amount;
        return i;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
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

    public BigDecimal getAmount() {
        return amount;
    }
}
