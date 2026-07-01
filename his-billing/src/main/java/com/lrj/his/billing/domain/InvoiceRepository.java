package com.lrj.his.billing.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByEncounterId(Long encounterId);

    boolean existsByEncounterId(Long encounterId);

    List<Invoice> findByPatientId(Long patientId);
}
