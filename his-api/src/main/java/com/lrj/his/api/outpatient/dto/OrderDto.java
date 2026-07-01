package com.lrj.his.api.outpatient.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 医嘱对外视图。字段与 his-outpatient 的 OrderVo 对齐,供 Feign 反序列化。
 */
public record OrderDto(
        Long id,
        Long encounterId,
        String orderType,
        String itemCode,
        String itemName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal amount,
        String state) implements Serializable {
}
