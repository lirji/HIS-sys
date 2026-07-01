package com.lrj.his.notify.domain;

/**
 * 通知类型。*_TODO 为角色待办(谁都能领),*_RESULT 为投递给具体人的结果通知。
 */
public enum NotificationType {
    RX_REVIEW_TODO,    // 处方待审(药师待办)
    RX_REVIEW_RESULT,  // 处方审核结果(通知开方医生)
    REFUND_TODO,       // 退费待审批(审批人待办)
    REFUND_RESULT      // 退费审批结果(通知申请人)
}
