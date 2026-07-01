package com.lrj.his.common.exception;

/**
 * 全平台统一错误码。区段划分:
 * 0      成功
 * 1xxx   通用/参数/鉴权
 * 2xxx   主数据 (mdm)
 * 3xxx   挂号 (registration)
 * 4xxx   门诊/医嘱 (outpatient)
 * 5xxx   收费 (billing)
 * 6xxx   病历 (emr)
 */
public enum ResultCode {

    OK(0, "OK"),

    // 通用
    BAD_REQUEST(1000, "请求参数错误"),
    UNAUTHORIZED(1001, "未认证或登录已过期"),
    FORBIDDEN(1002, "无权限访问"),
    NOT_FOUND(1003, "资源不存在"),
    CONFLICT(1004, "资源状态冲突"),
    TOO_MANY_REQUESTS(1429, "请求过于频繁,已被限流"),
    INTERNAL_ERROR(1500, "系统内部错误"),
    REMOTE_CALL_ERROR(1501, "下游服务调用失败"),

    // 主数据
    PATIENT_NOT_FOUND(2001, "患者不存在"),
    PATIENT_DUPLICATED(2002, "患者主索引重复(疑似同一人)"),
    DICTIONARY_NOT_FOUND(2003, "字典项不存在"),

    // 挂号
    SCHEDULE_NOT_FOUND(3001, "排班不存在"),
    SLOT_SOLD_OUT(3002, "号源已挂满"),
    APPOINTMENT_DUPLICATED(3003, "重复挂号"),

    // 门诊/医嘱
    ENCOUNTER_NOT_FOUND(4001, "就诊记录不存在"),
    ORDER_NOT_FOUND(4002, "医嘱不存在"),
    ORDER_STATE_ILLEGAL(4003, "医嘱状态流转非法"),
    RX_REVIEW_NOT_PENDING(4004, "无待审处方"),
    ORDER_NOT_REJECTED(4005, "医嘱非驳回态"),

    // 收费
    INVOICE_NOT_FOUND(5001, "账单不存在"),
    INVOICE_ALREADY_PAID(5002, "账单已支付"),
    REFUND_NOT_ALLOWED(5003, "当前账单不可退费"),
    REFUND_REQUEST_NOT_FOUND(5004, "退费申请不存在"),
    REFUND_NOT_PENDING(5005, "退费申请非待审态"),

    // 病历
    DOCUMENT_NOT_FOUND(6001, "病历文档不存在"),

    // 通知
    NOTIFICATION_NOT_FOUND(7001, "通知不存在");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
