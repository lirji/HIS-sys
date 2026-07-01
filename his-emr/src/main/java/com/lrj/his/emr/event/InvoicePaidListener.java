package com.lrj.his.emr.event;

import com.lrj.his.api.event.InvoicePaidEvent;
import com.lrj.his.api.event.Topics;
import com.lrj.his.emr.service.EmrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 监听账单支付事件 → 接诊计费闭环完成后,自动生成该次接诊的 FHIR 病历文档。
 * 独立消费组 his-emr,与 outpatient(推进医嘱执行)互不影响。
 */
@Component
public class InvoicePaidListener {

    private static final Logger log = LoggerFactory.getLogger(InvoicePaidListener.class);

    private final EmrService emrService;

    public InvoicePaidListener(EmrService emrService) {
        this.emrService = emrService;
    }

    @KafkaListener(topics = Topics.INVOICE_PAID, groupId = "his-emr")
    public void onInvoicePaid(InvoicePaidEvent event) {
        log.info("收到账单支付事件 invoiceId={} encounterId={}, 生成病历文档",
                event.invoiceId(), event.encounterId());
        emrService.generate(event.encounterId());
    }
}
