package com.lrj.his.billing.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {

    List<RefundRequest> findByStatusOrderByCreatedAtAsc(RefundStatus status);
}
