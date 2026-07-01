package com.lrj.his.api.registration;

import com.lrj.his.api.registration.dto.AppointmentDto;
import com.lrj.his.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * 挂号服务契约。门诊服务接诊时取挂号信息并回写"已就诊"。
 */
@FeignClient(name = "his-registration", path = "/reg/appointments", contextId = "registrationApi")
public interface RegistrationApi {

    @GetMapping("/{id}")
    Result<AppointmentDto> getById(@PathVariable("id") Long id);

    @PutMapping("/{id}/visited")
    Result<Void> markVisited(@PathVariable("id") Long id);
}
