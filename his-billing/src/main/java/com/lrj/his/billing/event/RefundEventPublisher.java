package com.lrj.his.billing.event;

import com.lrj.his.api.event.RefundRequestedEvent;
import com.lrj.his.api.event.RefundReviewedEvent;
import com.lrj.his.api.event.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 退费事件发布。按账单分区(key=invoiceId)。
 */
@Component
public class RefundEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RefundEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RefundEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishRequested(RefundRequestedEvent event) {
        kafkaTemplate.send(Topics.REFUND_REQUESTED, String.valueOf(event.invoiceId()), event);
        log.info("发布退费申请事件 refundRequestId={} invoiceId={}", event.refundRequestId(), event.invoiceId());
    }

    public void publishReviewed(RefundReviewedEvent event) {
        kafkaTemplate.send(Topics.REFUND_REVIEWED, String.valueOf(event.invoiceId()), event);
        log.info("发布退费审批结果事件 refundRequestId={} approved={}", event.refundRequestId(), event.approved());
    }
}
