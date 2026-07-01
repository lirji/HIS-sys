package com.lrj.his.outpatient.domain;

/**
 * 医嘱状态机事件(触发流转的动作)。
 */
public enum OrderEvent {
    SUBMIT,            // 提交医嘱(非药品,直达 SUBMITTED)
    SUBMIT_FOR_REVIEW, // 提交药品处方送审(→ PENDING_REVIEW)
    REVIEW_PASS,       // 药师审方通过
    REVIEW_REJECT,     // 药师审方驳回
    RESUBMIT,          // 驳回后重新送审
    EXECUTE,           // 缴费后执行
    STOP,              // 停止
    CANCEL,            // 作废
    REFUND             // 退费撤销(已执行 → 作废)
}
