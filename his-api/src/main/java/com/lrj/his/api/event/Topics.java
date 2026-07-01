package com.lrj.his.api.event;

/**
 * 平台 Kafka 主题常量。事件驱动 Saga 的契约。
 */
public final class Topics {

    /** 医嘱提交 → 计费(outpatient 发, billing 收) */
    public static final String ORDER_PLACED = "his.order.placed";

    /** 账单已支付 → 医嘱置为执行(billing 发, outpatient 收) */
    public static final String INVOICE_PAID = "his.invoice.paid";

    /** 处方待药师审核(outpatient 发, notify 收 → 药师待办) */
    public static final String RX_REVIEW_REQUESTED = "his.rx.review.requested";

    /** 处方审核结果:通过/驳回(outpatient 发, notify 收 → 通知医生) */
    public static final String RX_REVIEWED = "his.rx.reviewed";

    /** 退费申请(billing 发, notify 收 → 审批人待办) */
    public static final String REFUND_REQUESTED = "his.refund.requested";

    /** 退费审批结果(billing 发, notify 收 → 通知申请人;outpatient 收 → 撤销医嘱) */
    public static final String REFUND_REVIEWED = "his.refund.reviewed";

    private Topics() {
    }
}
