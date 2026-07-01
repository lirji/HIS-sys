package com.lrj.his.outpatient.statemachine;

import com.lrj.his.outpatient.domain.OrderEvent;
import com.lrj.his.outpatient.domain.OrderState;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

/**
 * 医嘱状态机定义。流转规则集中在此声明,业务代码只发事件,不写 if-else 判状态。
 *
 * <pre>
 *   CREATED --SUBMIT--> SUBMITTED --EXECUTE--> EXECUTED
 *      |  \--CANCEL--> CANCELLED      |  \--CANCEL--> CANCELLED
 *      |                             \--STOP----> STOPPED
 * </pre>
 */
@EnableStateMachineFactory
public class OrderStateMachineConfig extends StateMachineConfigurerAdapter<OrderState, OrderEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<OrderState, OrderEvent> states) throws Exception {
        // EXECUTED 不再标 end:退费需从 EXECUTED 转出到 CANCELLED。
        states.withStates()
                .initial(OrderState.CREATED)
                .states(EnumSet.allOf(OrderState.class))
                .end(OrderState.STOPPED)
                .end(OrderState.CANCELLED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions) throws Exception {
        transitions
                // 非药品:直达提交
                .withExternal().source(OrderState.CREATED).target(OrderState.SUBMITTED).event(OrderEvent.SUBMIT)
                .and()
                // 药品:送药师审方
                .withExternal().source(OrderState.CREATED).target(OrderState.PENDING_REVIEW).event(OrderEvent.SUBMIT_FOR_REVIEW)
                .and()
                .withExternal().source(OrderState.PENDING_REVIEW).target(OrderState.SUBMITTED).event(OrderEvent.REVIEW_PASS)
                .and()
                .withExternal().source(OrderState.PENDING_REVIEW).target(OrderState.REJECTED).event(OrderEvent.REVIEW_REJECT)
                .and()
                .withExternal().source(OrderState.REJECTED).target(OrderState.PENDING_REVIEW).event(OrderEvent.RESUBMIT)
                .and()
                .withExternal().source(OrderState.REJECTED).target(OrderState.CANCELLED).event(OrderEvent.CANCEL)
                .and()
                .withExternal().source(OrderState.CREATED).target(OrderState.CANCELLED).event(OrderEvent.CANCEL)
                .and()
                .withExternal().source(OrderState.SUBMITTED).target(OrderState.EXECUTED).event(OrderEvent.EXECUTE)
                .and()
                .withExternal().source(OrderState.SUBMITTED).target(OrderState.STOPPED).event(OrderEvent.STOP)
                .and()
                .withExternal().source(OrderState.SUBMITTED).target(OrderState.CANCELLED).event(OrderEvent.CANCEL)
                .and()
                // 退费撤销:已执行 → 作废
                .withExternal().source(OrderState.EXECUTED).target(OrderState.CANCELLED).event(OrderEvent.REFUND);
    }
}
