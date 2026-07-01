package com.lrj.his.api.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 医嘱提交事件。一次接诊提交的全部医嘱聚合为一条事件,billing 据此生成一张待缴费账单。
 */
public record OrderPlacedEvent(
        Long encounterId,
        Long patientId,
        String deptCode,
        List<OrderLine> items) implements Serializable {

    /** 账单行:一条医嘱对应一个计费项。 */
    public record OrderLine(
            Long orderId,
            String itemCode,
            String itemName,
            int quantity,
            BigDecimal amount) implements Serializable {
    }
}
