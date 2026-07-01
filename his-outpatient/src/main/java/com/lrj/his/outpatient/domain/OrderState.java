package com.lrj.his.outpatient.domain;

/**
 * 医嘱状态。
 * 非药品: CREATED →SUBMIT→ SUBMITTED →EXECUTE→ EXECUTED
 * 药品(需审方): CREATED →SUBMIT_FOR_REVIEW→ PENDING_REVIEW →REVIEW_PASS→ SUBMITTED →EXECUTE→ EXECUTED
 *                                              \→REVIEW_REJECT→ REJECTED →RESUBMIT→ PENDING_REVIEW / →CANCEL→ CANCELLED
 * 旁路: CREATED/SUBMITTED →CANCEL→ CANCELLED;SUBMITTED →STOP→ STOPPED;EXECUTED →REFUND→ CANCELLED(退费撤销)
 */
public enum OrderState {
    CREATED,        // 已开立(草稿)
    PENDING_REVIEW, // 待药师审方(药品处方)
    REJECTED,       // 审方驳回
    SUBMITTED,      // 已提交(已发计费事件,待缴费)
    EXECUTED,       // 已执行(缴费后)
    STOPPED,        // 已停止
    CANCELLED       // 已作废
}
