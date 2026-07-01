package com.lrj.his.api.event;

import java.io.Serializable;
import java.util.List;

/**
 * 账单支付事件。outpatient 收到后把对应医嘱从 SUBMITTED 推进到 EXECUTED,闭环完成。
 */
public record InvoicePaidEvent(
        Long invoiceId,
        Long encounterId,
        List<Long> orderIds) implements Serializable {
}
