package com.lrj.his.outpatient.statemachine;

import com.lrj.his.common.exception.BusinessException;
import com.lrj.his.common.exception.ResultCode;
import com.lrj.his.outpatient.domain.OrderEvent;
import com.lrj.his.outpatient.domain.OrderState;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 医嘱状态流转校验器。把当前状态重置进一台新机器、发事件,接受则返回目标状态,
 * 拒绝则抛 ORDER_STATE_ILLEGAL。转移规则全部来自 {@link OrderStateMachineConfig}。
 */
@Service
public class OrderStateMachineService {

    private final StateMachineFactory<OrderState, OrderEvent> factory;

    public OrderStateMachineService(StateMachineFactory<OrderState, OrderEvent> factory) {
        this.factory = factory;
    }

    public OrderState fire(OrderState current, OrderEvent event) {
        StateMachine<OrderState, OrderEvent> sm = factory.getStateMachine();
        sm.stopReactively().block();
        sm.getStateMachineAccessor().doWithAllRegions(access ->
                access.resetStateMachineReactively(
                        new DefaultStateMachineContext<>(current, null, null, null)).block());
        sm.startReactively().block();

        StateMachineEventResult<OrderState, OrderEvent> result = sm.sendEvent(
                Mono.just(MessageBuilder.withPayload(event).build())).blockLast();

        if (result == null
                || result.getResultType() == StateMachineEventResult.ResultType.DENIED) {
            sm.stopReactively().block();
            throw BusinessException.of(ResultCode.ORDER_STATE_ILLEGAL,
                    "医嘱状态 " + current + " 不允许动作 " + event);
        }
        OrderState target = sm.getState().getId();
        sm.stopReactively().block();
        return target;
    }
}
