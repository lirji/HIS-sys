package com.lrj.his.outpatient.event;

import com.lrj.his.api.event.OrderPlacedEvent;
import com.lrj.his.api.event.RxReviewRequestedEvent;
import com.lrj.his.api.event.RxReviewedEvent;
import com.lrj.his.api.event.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 医嘱事件发布。按患者分区(key=patientId),保证同一患者事件有序。
 */
@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderPlaced(OrderPlacedEvent event) {
        kafkaTemplate.send(Topics.ORDER_PLACED, String.valueOf(event.patientId()), event);
        log.info("发布医嘱事件 encounterId={} 项数={}", event.encounterId(), event.items().size());
    }

    public void publishRxReviewRequested(RxReviewRequestedEvent event) {
        kafkaTemplate.send(Topics.RX_REVIEW_REQUESTED, String.valueOf(event.patientId()), event);
        log.info("发布处方待审事件 encounterId={} 药品数={}", event.encounterId(), event.drugItemCount());
    }

    public void publishRxReviewed(RxReviewedEvent event) {
        kafkaTemplate.send(Topics.RX_REVIEWED, String.valueOf(event.patientId()), event);
        log.info("发布处方审核结果事件 encounterId={} 通过={}", event.encounterId(), event.passed());
    }
}
