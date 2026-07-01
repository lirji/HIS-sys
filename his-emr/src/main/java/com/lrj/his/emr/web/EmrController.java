package com.lrj.his.emr.web;

import com.lrj.his.common.web.Result;
import com.lrj.his.emr.service.EmrService;
import com.lrj.his.security.rbac.RequiresRole;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 病历文档管理。生成限医生角色;读取返回已落库的 FHIR Bundle JSON。
 */
@RestController
@RequestMapping("/emr/documents")
public class EmrController {

    static final String FHIR_JSON = "application/fhir+json";

    private final EmrService emrService;

    public EmrController(EmrService emrService) {
        this.emrService = emrService;
    }

    /** 手动触发生成/刷新病历文档(计费闭环也会经 Kafka 自动生成)。 */
    @PostMapping("/{encounterId}")
    @RequiresRole({"DOCTOR"})
    public Result<Long> generate(@PathVariable("encounterId") Long encounterId) {
        return Result.ok(emrService.generate(encounterId).getId());
    }

    /** 取已落库的门诊病历文档(FHIR Bundle)。 */
    @GetMapping(value = "/{encounterId}", produces = FHIR_JSON + ";charset=UTF-8")
    public String get(@PathVariable("encounterId") Long encounterId) {
        return emrService.getDocumentContent(encounterId);
    }
}
