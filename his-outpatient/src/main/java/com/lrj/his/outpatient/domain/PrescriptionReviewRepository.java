package com.lrj.his.outpatient.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionReviewRepository extends JpaRepository<PrescriptionReview, Long> {

    List<PrescriptionReview> findByEncounterIdOrderByReviewedAtDesc(Long encounterId);
}
