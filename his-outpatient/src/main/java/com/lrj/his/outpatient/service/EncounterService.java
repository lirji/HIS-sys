package com.lrj.his.outpatient.service;

import com.lrj.his.api.event.OrderPlacedEvent;
import com.lrj.his.api.event.RxReviewRequestedEvent;
import com.lrj.his.api.registration.RegistrationApi;
import com.lrj.his.api.registration.dto.AppointmentDto;
import com.lrj.his.common.exception.BusinessException;
import com.lrj.his.common.exception.ResultCode;
import com.lrj.his.common.web.Result;
import com.lrj.his.outpatient.domain.Encounter;
import com.lrj.his.outpatient.domain.EncounterRepository;
import com.lrj.his.outpatient.domain.Order;
import com.lrj.his.outpatient.domain.OrderEvent;
import com.lrj.his.outpatient.domain.OrderRepository;
import com.lrj.his.outpatient.domain.OrderState;
import com.lrj.his.outpatient.domain.OrderType;
import com.lrj.his.outpatient.event.OrderEventPublisher;
import com.lrj.his.outpatient.statemachine.OrderStateMachineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EncounterService {

    private static final Logger log = LoggerFactory.getLogger(EncounterService.class);

    private final EncounterRepository encounterRepository;
    private final OrderRepository orderRepository;
    private final OrderStateMachineService stateMachine;
    private final OrderEventPublisher publisher;
    private final RegistrationApi registrationApi;

    public EncounterService(EncounterRepository encounterRepository,
                            OrderRepository orderRepository,
                            OrderStateMachineService stateMachine,
                            OrderEventPublisher publisher,
                            RegistrationApi registrationApi) {
        this.encounterRepository = encounterRepository;
        this.orderRepository = orderRepository;
        this.stateMachine = stateMachine;
        this.publisher = publisher;
        this.registrationApi = registrationApi;
    }

    /** 接诊:据挂号记录建立就诊,并回写挂号"已就诊"。 */
    @Transactional
    public Encounter openEncounter(Long appointmentId, Long doctorId, String doctorName) {
        AppointmentDto appt = fetchAppointment(appointmentId);
        Encounter encounter = Encounter.open(appointmentId, appt.patientId(), appt.deptCode(),
                doctorId, doctorName);
        encounter = encounterRepository.save(encounter);
        safeMarkVisited(appointmentId);
        return encounter;
    }

    @Transactional
    public Encounter recordDiagnosis(Long encounterId, String chiefComplaint,
                                     String diagnosisCode, String diagnosisName) {
        Encounter e = loadEncounter(encounterId);
        e.recordDiagnosis(chiefComplaint, diagnosisCode, diagnosisName);
        return encounterRepository.save(e);
    }

    /** 开医嘱(草稿)。仅就诊中可开。 */
    @Transactional
    public Order addOrder(Long encounterId, OrderType type, String itemCode, String itemName,
                          int quantity, BigDecimal unitPrice) {
        Encounter e = loadEncounter(encounterId);
        if (!e.isOpen()) {
            throw BusinessException.of(ResultCode.CONFLICT, "就诊已提交,不能再开医嘱");
        }
        Order order = Order.create(encounterId, e.getPatientId(), type, itemCode, itemName, quantity, unitPrice);
        return orderRepository.save(order);
    }

    /**
     * 提交医嘱:药品医嘱送药师审方(→ PENDING_REVIEW),其余直达 SUBMITTED。
     * 含药品时发 RxReviewRequestedEvent 触发药师待办;计费推迟到无待审医嘱时由 {@link #tryBill} 触发。
     */
    @Transactional
    public void submitOrders(Long encounterId) {
        Encounter e = loadEncounter(encounterId);
        if (!e.isOpen()) {
            throw BusinessException.of(ResultCode.CONFLICT, "就诊已提交");
        }
        List<Order> drafts = orderRepository.findByEncounterIdAndState(encounterId, OrderState.CREATED);
        if (drafts.isEmpty()) {
            throw BusinessException.of(ResultCode.ORDER_NOT_FOUND, "无可提交的医嘱");
        }
        int drugCount = 0;
        for (Order o : drafts) {
            OrderEvent ev = o.getOrderType() == OrderType.DRUG ? OrderEvent.SUBMIT_FOR_REVIEW : OrderEvent.SUBMIT;
            o.applyState(stateMachine.fire(o.getState(), ev));
            orderRepository.save(o);
            if (o.getOrderType() == OrderType.DRUG) {
                drugCount++;
            }
        }
        e.markSubmitted();
        encounterRepository.save(e);

        if (drugCount > 0) {
            publisher.publishRxReviewRequested(new RxReviewRequestedEvent(
                    encounterId, e.getPatientId(), e.getDeptCode(), e.getDoctorId(), e.getDoctorName(), drugCount));
        }
        tryBill(encounterId);
    }

    /**
     * 计费守卫:就诊不再有"待处理"医嘱(草稿/待审/驳回)时,把全部 SUBMITTED 医嘱
     * 一次性发 OrderPlacedEvent 计费(事件驱动 Saga 第一段)。billed 标志 + billing 端 encounterId
     * 唯一双重保证不重复建单。无药品时由 submitOrders 末尾即时触发;有药品时由审方通过/驳回后触发。
     */
    @Transactional
    public void tryBill(Long encounterId) {
        Encounter e = loadEncounter(encounterId);
        if (e.isBilled()) {
            return;
        }
        boolean hasPending = orderRepository.existsByEncounterIdAndStateIn(encounterId,
                List.of(OrderState.CREATED, OrderState.PENDING_REVIEW, OrderState.REJECTED));
        if (hasPending) {
            return;
        }
        List<Order> submitted = orderRepository.findByEncounterIdAndState(encounterId, OrderState.SUBMITTED);
        if (submitted.isEmpty()) {
            return;
        }
        List<OrderPlacedEvent.OrderLine> lines = submitted.stream().map(o ->
                new OrderPlacedEvent.OrderLine(o.getId(), o.getItemCode(), o.getItemName(),
                        o.getQuantity(), o.getAmount())).toList();
        e.markBilled();
        encounterRepository.save(e);
        publisher.publishOrderPlaced(new OrderPlacedEvent(
                encounterId, e.getPatientId(), e.getDeptCode(), lines));
    }

    /** 退费撤销:把账单覆盖的已执行医嘱推进到 CANCELLED。幂等(非 EXECUTED 跳过)。 */
    @Transactional
    public void cancelExecutedOrders(List<Long> orderIds) {
        for (Long id : orderIds) {
            orderRepository.findById(id).ifPresent(o -> {
                if (o.getState() == OrderState.EXECUTED) {
                    o.applyState(stateMachine.fire(o.getState(), OrderEvent.REFUND));
                    orderRepository.save(o);
                }
            });
        }
    }

    /**
     * 缴费回调:把指定医嘱从 SUBMITTED 推进到 EXECUTED(事件驱动 Saga 第二段)。
     * 由 InvoicePaidListener 调用,幂等(已 EXECUTED 的跳过)。
     */
    @Transactional
    public void executeOrders(List<Long> orderIds) {
        for (Long id : orderIds) {
            orderRepository.findById(id).ifPresent(o -> {
                if (o.getState() == OrderState.SUBMITTED) {
                    o.applyState(stateMachine.fire(o.getState(), OrderEvent.EXECUTE));
                    orderRepository.save(o);
                }
            });
        }
    }

    /** 医生处理被驳回处方:重新送审(REJECTED → PENDING_REVIEW),并再次通知药师。 */
    @Transactional
    public void resubmitRejectedOrder(Long encounterId, Long orderId) {
        Order o = loadOrder(encounterId, orderId);
        if (o.getState() != OrderState.REJECTED) {
            throw BusinessException.of(ResultCode.ORDER_NOT_REJECTED);
        }
        o.applyState(stateMachine.fire(o.getState(), OrderEvent.RESUBMIT));
        orderRepository.save(o);
        Encounter e = loadEncounter(encounterId);
        publisher.publishRxReviewRequested(new RxReviewRequestedEvent(
                encounterId, e.getPatientId(), e.getDeptCode(), e.getDoctorId(), e.getDoctorName(), 1));
    }

    /** 医生作废草稿/被驳回医嘱;作废后尝试结算(可能解除计费阻塞)。 */
    @Transactional
    public void cancelOrder(Long encounterId, Long orderId) {
        Order o = loadOrder(encounterId, orderId);
        if (o.getState() != OrderState.REJECTED && o.getState() != OrderState.CREATED) {
            throw BusinessException.of(ResultCode.ORDER_STATE_ILLEGAL, "仅草稿/驳回的医嘱可作废");
        }
        o.applyState(stateMachine.fire(o.getState(), OrderEvent.CANCEL));
        orderRepository.save(o);
        tryBill(encounterId);
    }

    @Transactional(readOnly = true)
    public List<Order> listOrders(Long encounterId) {
        return orderRepository.findByEncounterId(encounterId);
    }

    @Transactional(readOnly = true)
    public Encounter getEncounter(Long encounterId) {
        return loadEncounter(encounterId);
    }

    private Encounter loadEncounter(Long id) {
        return encounterRepository.findById(id)
                .orElseThrow(() -> BusinessException.of(ResultCode.ENCOUNTER_NOT_FOUND));
    }

    private Order loadOrder(Long encounterId, Long orderId) {
        Order o = orderRepository.findById(orderId)
                .orElseThrow(() -> BusinessException.of(ResultCode.ORDER_NOT_FOUND));
        if (!o.getEncounterId().equals(encounterId)) {
            throw BusinessException.of(ResultCode.ORDER_NOT_FOUND, "医嘱不属于该就诊");
        }
        return o;
    }

    private AppointmentDto fetchAppointment(Long appointmentId) {
        try {
            Result<AppointmentDto> resp = registrationApi.getById(appointmentId);
            if (resp == null || !resp.success() || resp.data() == null) {
                throw BusinessException.of(ResultCode.NOT_FOUND, "挂号记录不存在");
            }
            return resp.data();
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            log.warn("调用 his-registration 取挂号失败 appointmentId={}", appointmentId, ex);
            throw BusinessException.of(ResultCode.REMOTE_CALL_ERROR, "挂号服务不可用");
        }
    }

    private void safeMarkVisited(Long appointmentId) {
        try {
            registrationApi.markVisited(appointmentId);
        } catch (Exception ex) {
            log.warn("回写挂号已就诊失败 appointmentId={}(不阻断接诊)", appointmentId, ex);
        }
    }
}
