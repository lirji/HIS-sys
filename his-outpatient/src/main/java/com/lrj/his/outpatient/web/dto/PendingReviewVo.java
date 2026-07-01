package com.lrj.his.outpatient.web.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 药师待审工作台条目:一次就诊 + 其待审药品明细。
 */
public record PendingReviewVo(
        Long encounterId,
        Long patientId,
        String deptCode,
        String doctorName,
        List<Item> items) {

    public record Item(Long orderId, String itemCode, String itemName, int quantity, BigDecimal amount) {
    }
}
