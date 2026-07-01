package com.lrj.his.common.exception;

/**
 * 业务异常。携带 ResultCode,由 GlobalExceptionHandler 统一转 Result。
 */
public class BusinessException extends RuntimeException {

    private final ResultCode resultCode;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    public static BusinessException of(ResultCode rc) {
        return new BusinessException(rc);
    }

    public static BusinessException of(ResultCode rc, String message) {
        return new BusinessException(rc, message);
    }
}
