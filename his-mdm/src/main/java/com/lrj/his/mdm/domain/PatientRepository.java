package com.lrj.his.mdm.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByIdCard(String idCard);

    Optional<Patient> findByIdCard(String idCard);

    Optional<Patient> findByEmpiNo(String empiNo);
}
