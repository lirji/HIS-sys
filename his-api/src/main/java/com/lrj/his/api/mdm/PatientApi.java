package com.lrj.his.api.mdm;

import com.lrj.his.api.mdm.dto.PatientDto;
import com.lrj.his.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 主数据服务 — 患者主索引契约。registration / outpatient / billing 通过它查患者。
 * 通过 Nacos 服务名 his-mdm 发现并负载均衡。
 */
@FeignClient(name = "his-mdm", path = "/mdm/patients", contextId = "patientApi")
public interface PatientApi {

    @GetMapping("/{id}")
    Result<PatientDto> getById(@PathVariable("id") Long id);
}
