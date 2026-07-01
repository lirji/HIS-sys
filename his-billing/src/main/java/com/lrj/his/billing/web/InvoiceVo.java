package com.lrj.his.billing.web;

import com.lrj.his.billing.domain.Invoice;
import com.lrj.his.billing.domain.InvoiceItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record InvoiceVo(
        Long id,
        Long encounterId,
        Long patientId,
        String deptCode,
        BigDecimal totalAmount,
        String status,
        String payMethod,
        Instant paidAt,
        List<Line> items) {

    public record Line(Long orderId, String itemCode, String itemName, int quantity, BigDecimal amount) {
        static Line from(InvoiceItem i) {
            return new Line(i.getOrderId(), i.getItemCode(), i.getItemName(), i.getQuantity(), i.getAmount());
        }
    }

    public static InvoiceVo from(Invoice inv) {
        return new InvoiceVo(inv.getId(), inv.getEncounterId(), inv.getPatientId(), inv.getDeptCode(),
                inv.getTotalAmount(), inv.getStatus().name(), inv.getPayMethod(), inv.getPaidAt(),
                inv.getItems().stream().map(Line::from).toList());
    }
}
