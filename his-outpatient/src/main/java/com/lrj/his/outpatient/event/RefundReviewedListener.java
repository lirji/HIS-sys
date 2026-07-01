package com.lrj.his.outpatient.event;

import com.lrj.his.api.event.RefundReviewedEvent;
import com.lrj.his.api.event.Topics;
import com.lrj.his.outpatient.service.EncounterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 监听退费审批结果 → 通过时撤销账单覆盖的已执行医嘱(闭环退费副作用)。
 */
@Component
public class RefundReviewedListener {

    private static final Logger log = LoggerFactory.getLogger(RefundReviewedListener.class);

    private final EncounterService encounterService;

    public RefundReviewedListener(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @KafkaListener(topics = Topics.REFUND_REVIEWED, groupId = "his-outpatient")
    public void onRefundReviewed(RefundReviewedEvent event) {
        log.info("收到退费审批结果 refundRequestId={} encounterId={} approved={}",
                event.refundRequestId(), event.encounterId(), event.approved());
        if (event.approved() && event.orderIds() != null) {
            encounterService.cancelExecutedOrders(event.orderIds());
        }
    }
}
