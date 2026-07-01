package com.lrj.his.billing.domain;

public enum InvoiceStatus {
    UNPAID,   // 待缴费
    PAID,     // 已缴费
    REFUNDED, // 已退费
    CANCELLED // 已取消
}
