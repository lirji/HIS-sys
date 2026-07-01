package com.lrj.his.billing.event;

import com.lrj.his.api.event.InvoicePaidEvent;
import com.lrj.his.api.event.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InvoicePaidPublisher {

    private static final Logger log = LoggerFactory.getLogger(InvoicePaidPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InvoicePaidPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(InvoicePaidEvent event) {
        kafkaTemplate.send(Topics.INVOICE_PAID, String.valueOf(event.invoiceId()), event);
        log.info("发布账单支付事件 invoiceId={} 医嘱数={}", event.invoiceId(), event.orderIds().size());
    }
}
