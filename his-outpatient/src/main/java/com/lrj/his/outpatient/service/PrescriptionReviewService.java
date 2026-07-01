package com.lrj.his.outpatient.service;

import com.lrj.his.api.event.RxReviewedEvent;
import com.lrj.his.common.context.CurrentUser;
import com.lrj.his.common.exception.BusinessException;
import com.lrj.his.common.exception.ResultCode;
import com.lrj.his.outpatient.domain.Encounter;
import com.lrj.his.outpatient.domain.EncounterRepository;
import com.lrj.his.outpatient.domain.Order;
import com.lrj.his.outpatient.domain.OrderEvent;
import com.lrj.his.outpatient.domain.OrderRepository;
import com.lrj.his.outpatient.domain.OrderState;
import com.lrj.his.outpatient.domain.PrescriptionReview;
import com.lrj.his.outpatient.domain.PrescriptionReviewRepository;
import com.lrj.his.outpatient.domain.ReviewResult;
import com.lrj.his.outpatient.event.OrderEventPublisher;
import com.lrj.his.outpatient.statemachine.OrderStateMachineService;
import com.lrj.his.outpatient.web.dto.PendingReviewVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 药师审方审批流。处方(药品医嘱)提交后进入 PENDING_REVIEW,药师按就诊批量通过/驳回。
 * 通过 → 推进到 SUBMITTED 并触发计费;驳回 → REJECTED,等待医生重提或作废。
 */
@Service
public class PrescriptionReviewService {

    private final OrderRepository orderRepository;
    private final EncounterRepository encounterRepository;
    private final PrescriptionReviewRepository reviewRepository;
    private final OrderStateMachineService stateMachine;
    private final OrderEventPublisher publisher;
    private final EncounterService encounterService;

    public PrescriptionReviewService(OrderRepository orderRepository,
                                     EncounterRepository encounterRepository,
                                     PrescriptionReviewRepository reviewRepository,
                                     OrderStateMachineService stateMachine,
                                     OrderEventPublisher publisher,
                                     EncounterService encounterService) {
        this.orderRepository = orderRepository;
        this.encounterRepository = encounterRepository;
        this.reviewRepository = reviewRepository;
        this.stateMachine = stateMachine;
        this.publisher = publisher;
        this.encounterService = encounterService;
    }

    /** 待审工作台:全部 PENDING_REVIEW 药嘱按就诊聚合。 */
    @Transactional(readOnly = true)
    public List<PendingReviewVo> listPending() {
        Map<Long, List<Order>> byEncounter = orderRepository.findByState(OrderState.PENDING_REVIEW).stream()
                .collect(Collectors.groupingBy(Order::getEncounterId));
        return byEncounter.entrySet().stream().map(entry -> {
            Encounter e = encounterRepository.findById(entry.getKey()).orElse(null);
            List<PendingReviewVo.Item> items = entry.getValue().stream().map(o ->
                    new PendingReviewVo.Item(o.getId(), o.getItemCode(), o.getItemName(),
                            o.getQuantity(), o.getAmount())).toList();
            return new PendingReviewVo(entry.getKey(),
                    e != null ? e.getPatientId() : null,
                    e != null ? e.getDeptCode() : null,
                    e != null ? e.getDoctorName() : null,
                    items);
        }).toList();
    }

    /** 审方通过:该就诊全部待审药嘱 → SUBMITTED,留痕,通知医生,触发计费。 */
    @Transactional
    public void pass(Long encounterId, CurrentUser pharmacist) {
        Encounter e = loadEncounter(encounterId);
        List<Order> pending = orderRepository.findByEncounterIdAndState(encounterId, OrderState.PENDING_REVIEW);
        if (pending.isEmpty()) {
            throw BusinessException.of(ResultCode.RX_REVIEW_NOT_PENDING);
        }
        for (Order o : pending) {
            o.applyState(stateMachine.fire(o.getState(), OrderEvent.REVIEW_PASS));
            orderRepository.save(o);
        }
        reviewRepository.save(PrescriptionReview.of(encounterId, pharmacist.userId(),
                pharmacist.username(), ReviewResult.PASS, null));
        publisher.publishRxReviewed(new RxReviewedEvent(encounterId, e.getPatientId(),
                e.getDoctorId(), true, null, pharmacist.username()));
        encounterService.tryBill(encounterId);
    }

    /** 审方驳回:该就诊全部待审药嘱 → REJECTED,留痕(含意见),通知医生。不计费。 */
    @Transactional
    public void reject(Long encounterId, String opinion, CurrentUser pharmacist) {
        Encounter e = loadEncounter(encounterId);
        List<Order> pending = orderRepository.findByEncounterIdAndState(encounterId, OrderState.PENDING_REVIEW);
        if (pending.isEmpty()) {
            throw BusinessException.of(ResultCode.RX_REVIEW_NOT_PENDING);
        }
        for (Order o : pending) {
            o.applyState(stateMachine.fire(o.getState(), OrderEvent.REVIEW_REJECT));
            orderRepository.save(o);
        }
        reviewRepository.save(PrescriptionReview.of(encounterId, pharmacist.userId(),
                pharmacist.username(), ReviewResult.REJECT, opinion));
        publisher.publishRxReviewed(new RxReviewedEvent(encounterId, e.getPatientId(),
                e.getDoctorId(), false, opinion, pharmacist.username()));
    }

    private Encounter loadEncounter(Long id) {
        return encounterRepository.findById(id)
                .orElseThrow(() -> BusinessException.of(ResultCode.ENCOUNTER_NOT_FOUND));
    }
}
