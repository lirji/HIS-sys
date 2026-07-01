package com.lrj.his.common.web;

import com.lrj.his.common.exception.ResultCode;

import java.io.Serializable;
import java.time.Instant;

/**
 * 统一响应包装。所有 REST 接口返回 Result<T>。
 */
public record Result<T>(int code, String message, T data, Instant timestamp) implements Serializable {

    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.OK.getCode(), ResultCode.OK.getMessage(), data, Instant.now());
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(ResultCode rc) {
        return new Result<>(rc.getCode(), rc.getMessage(), null, Instant.now());
    }

    public static <T> Result<T> fail(ResultCode rc, String message) {
        return new Result<>(rc.getCode(), message, null, Instant.now());
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null, Instant.now());
    }

    public boolean success() {
        return code == ResultCode.OK.getCode();
    }
}
