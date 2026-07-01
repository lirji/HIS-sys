package com.lrj.his.outpatient.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByEncounterId(Long encounterId);

    List<Order> findByEncounterIdAndState(Long encounterId, OrderState state);

    List<Order> findByEncounterIdAndStateIn(Long encounterId, Collection<OrderState> states);

    /** 是否仍有"待处理"医嘱(草稿/待审/驳回),用于判断该就诊能否计费。 */
    boolean existsByEncounterIdAndStateIn(Long encounterId, Collection<OrderState> states);

    /** 跨就诊按状态查(药师待审工作台用 PENDING_REVIEW)。 */
    List<Order> findByState(OrderState state);
}
