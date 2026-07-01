package com.lrj.his.emr.web;

import com.lrj.his.emr.service.EmrService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FHIR R4 只读资源接口。返回标准 application/fhir+json,不套平台 Result 包装,
 * 以符合 FHIR REST 规范。资源由领域数据实时投影而成。
 */
@RestController
@RequestMapping(value = "/fhir", produces = "application/fhir+json;charset=UTF-8")
public class FhirController {

    private final EmrService emrService;

    public FhirController(EmrService emrService) {
        this.emrService = emrService;
    }

    @GetMapping("/Patient/{id}")
    public String patient(@PathVariable("id") Long id) {
        return emrService.patientResource(id);
    }

    @GetMapping("/Encounter/{id}")
    public String encounter(@PathVariable("id") Long id) {
        return emrService.encounterResource(id);
    }
}
