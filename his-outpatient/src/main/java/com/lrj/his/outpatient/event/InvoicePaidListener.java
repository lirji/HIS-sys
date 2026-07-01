package com.lrj.his.outpatient.event;

import com.lrj.his.api.event.InvoicePaidEvent;
import com.lrj.his.api.event.Topics;
import com.lrj.his.outpatient.service.EncounterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 监听账单支付事件 → 推进医嘱到 EXECUTED(闭环第二段)。
 */
@Component
public class InvoicePaidListener {

    private static final Logger log = LoggerFactory.getLogger(InvoicePaidListener.class);

    private final EncounterService encounterService;

    public InvoicePaidListener(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @KafkaListener(topics = Topics.INVOICE_PAID, groupId = "his-outpatient")
    public void onInvoicePaid(InvoicePaidEvent event) {
        log.info("收到账单支付事件 invoiceId={} encounterId={}", event.invoiceId(), event.encounterId());
        encounterService.executeOrders(event.orderIds());
    }
}
