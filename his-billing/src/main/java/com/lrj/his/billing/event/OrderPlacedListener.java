package com.lrj.his.billing.event;

import com.lrj.his.api.event.OrderPlacedEvent;
import com.lrj.his.api.event.Topics;
import com.lrj.his.billing.service.BillingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 监听医嘱提交事件 → 生成待缴费账单(闭环第一段消费端)。
 */
@Component
public class OrderPlacedListener {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacedListener.class);

    private final BillingService billingService;

    public OrderPlacedListener(BillingService billingService) {
        this.billingService = billingService;
    }

    @KafkaListener(topics = Topics.ORDER_PLACED, groupId = "his-billing")
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("收到医嘱事件 encounterId={} 项数={}", event.encounterId(), event.items().size());
        billingService.createFromOrderPlaced(event);
    }
}
