package com.lrj.his.emr.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClinicalDocumentRepository extends JpaRepository<ClinicalDocument, Long> {

    Optional<ClinicalDocument> findByEncounterId(Long encounterId);
}
