package com.lrj.his.mdm.web;

import com.lrj.his.api.mdm.dto.PatientDto;
import com.lrj.his.common.web.Result;
import com.lrj.his.mdm.service.PatientService;
import com.lrj.his.security.rbac.RequiresRole;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 患者主索引 REST。路径与 {@link com.lrj.his.api.mdm.PatientApi} 契约一致,
 * 供下游服务通过 Feign 调用。
 */
@RestController
@RequestMapping("/mdm/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /** 建档:仅管理员/护士(挂号台)可登记患者。 */
    @PostMapping
    @RequiresRole({"ADMIN", "NURSE"})
    public Result<PatientDto> create(@Valid @RequestBody CreatePatientRequest request) {
        return Result.ok(patientService.register(request));
    }

    @GetMapping("/{id}")
    public Result<PatientDto> getById(@PathVariable("id") Long id) {
        return Result.ok(patientService.getById(id));
    }
}
