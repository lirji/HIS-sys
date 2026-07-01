package com.lrj.his.common.web;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果。content + 总数 + 页码信息。
 */
public record PageResult<T>(List<T> content, long total, int page, int size) implements Serializable {

    public static <T> PageResult<T> of(List<T> content, long total, int page, int size) {
        return new PageResult<>(content, total, page, size);
    }

    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) total / size);
    }
}
