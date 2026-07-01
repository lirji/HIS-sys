package com.lrj.his.mdm.service;

import com.lrj.his.mdm.domain.Dictionary;
import com.lrj.his.mdm.domain.DictionaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DictionaryService {

    private final DictionaryRepository repository;

    public DictionaryService(DictionaryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Dictionary> listByType(String type) {
        return repository.findByTypeAndEnabledTrueOrderBySortNoAsc(type);
    }
}
