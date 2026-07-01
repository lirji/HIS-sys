package com.lrj.his.registration.web;

import com.lrj.his.api.registration.dto.AppointmentDto;
import com.lrj.his.common.web.Result;
import com.lrj.his.registration.domain.Schedule;
import com.lrj.his.registration.service.RegistrationService;
import com.lrj.his.security.rbac.RequiresRole;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reg")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /** 开诊排班:管理员维护号源池。 */
    @PostMapping("/schedules")
    @RequiresRole({"ADMIN"})
    public Result<Schedule> openSchedule(@Valid @RequestBody OpenScheduleRequest request) {
        return Result.ok(registrationService.openSchedule(request));
    }

    @GetMapping("/schedules")
    public Result<List<Schedule>> listSchedules(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String deptCode) {
        return Result.ok(registrationService.listSchedules(date, deptCode));
    }

    /** 挂号:挂号台(护士)或管理员操作。 */
    @PostMapping("/appointments")
    @RequiresRole({"ADMIN", "NURSE", "CASHIER"})
    public Result<AppointmentVo> book(@Valid @RequestBody BookRequest request) {
        return Result.ok(AppointmentVo.from(registrationService.book(request)));
    }

    /** 供 his-outpatient 通过 Feign 调用:取挂号信息。 */
    @GetMapping("/appointments/{id}")
    public Result<AppointmentDto> getAppointment(@PathVariable("id") Long id) {
        return Result.ok(registrationService.getAppointment(id));
    }

    /** 供 his-outpatient 接诊后回写已就诊。 */
    @PutMapping("/appointments/{id}/visited")
    public Result<Void> markVisited(@PathVariable("id") Long id) {
        registrationService.markVisited(id);
        return Result.ok();
    }
}
