package com.lrj.his.api.outpatient;

import com.lrj.his.api.outpatient.dto.EncounterDto;
import com.lrj.his.api.outpatient.dto.OrderDto;
import com.lrj.his.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 门诊服务契约。his-emr 据此取就诊 + 医嘱,组装 FHIR 病历文档。
 */
@FeignClient(name = "his-outpatient", path = "/outpatient/encounters", contextId = "outpatientApi")
public interface OutpatientApi {

    @GetMapping("/{id}")
    Result<EncounterDto> getEncounter(@PathVariable("id") Long id);

    @GetMapping("/{id}/orders")
    Result<List<OrderDto>> listOrders(@PathVariable("id") Long id);
}
