package com.lrj.his.mdm.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DictionaryRepository extends JpaRepository<Dictionary, Long> {

    List<Dictionary> findByTypeAndEnabledTrueOrderBySortNoAsc(String type);

    Optional<Dictionary> findByTypeAndCode(String type, String code);
}
