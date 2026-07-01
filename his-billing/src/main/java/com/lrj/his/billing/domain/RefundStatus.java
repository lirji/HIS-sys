package com.lrj.his.billing.domain;

public enum RefundStatus {
    REQUESTED, // 待审批
    APPROVED,  // 已通过(已退款)
    REJECTED   // 已驳回
}
