package com.lrj.his.outpatient.web.dto;

import com.lrj.his.outpatient.domain.Order;

import java.math.BigDecimal;

public record OrderVo(
        Long id,
        Long encounterId,
        String orderType,
        String itemCode,
        String itemName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal amount,
        String state) {

    public static OrderVo from(Order o) {
        return new OrderVo(o.getId(), o.getEncounterId(), o.getOrderType().name(),
                o.getItemCode(), o.getItemName(), o.getQuantity(),
                o.getUnitPrice(), o.getAmount(), o.getState().name());
    }
}
