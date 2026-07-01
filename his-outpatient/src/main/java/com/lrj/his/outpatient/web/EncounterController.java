package com.lrj.his.outpatient.web;

import com.lrj.his.common.context.CurrentUser;
import com.lrj.his.common.context.UserContext;
import com.lrj.his.common.exception.BusinessException;
import com.lrj.his.common.exception.ResultCode;
import com.lrj.his.common.web.Result;
import com.lrj.his.outpatient.domain.Encounter;
import com.lrj.his.outpatient.service.EncounterService;
import com.lrj.his.outpatient.web.dto.AddOrderRequest;
import com.lrj.his.outpatient.web.dto.EncounterVo;
import com.lrj.his.outpatient.web.dto.OpenEncounterRequest;
import com.lrj.his.outpatient.web.dto.OrderVo;
import com.lrj.his.outpatient.web.dto.RecordDiagnosisRequest;
import com.lrj.his.security.rbac.RequiresRole;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 门诊医生站。开方/接诊均限医生角色,医生身份取自登录上下文(网关透传)。
 */
@RestController
@RequestMapping("/outpatient/encounters")
public class EncounterController {

    private final EncounterService encounterService;

    public EncounterController(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    @PostMapping
    @RequiresRole({"DOCTOR"})
    public Result<EncounterVo> open(@Valid @RequestBody OpenEncounterRequest request) {
        CurrentUser doctor = currentDoctor();
        Encounter e = encounterService.openEncounter(request.appointmentId(),
                doctor.userId(), doctor.username());
        return Result.ok(EncounterVo.from(e));
    }

    @PutMapping("/{id}/diagnosis")
    @RequiresRole({"DOCTOR"})
    public Result<EncounterVo> diagnose(@PathVariable("id") Long id,
                                        @Valid @RequestBody RecordDiagnosisRequest request) {
        Encounter e = encounterService.recordDiagnosis(id, request.chiefComplaint(),
                request.diagnosisCode(), request.diagnosisName());
        return Result.ok(EncounterVo.from(e));
    }

    @PostMapping("/{id}/orders")
    @RequiresRole({"DOCTOR"})
    public Result<OrderVo> addOrder(@PathVariable("id") Long id,
                                    @Valid @RequestBody AddOrderRequest request) {
        return Result.ok(OrderVo.from(encounterService.addOrder(id, request.orderType(),
                request.itemCode(), request.itemName(), request.quantity(), request.unitPrice())));
    }

    /** 提交医嘱 → 触发计费事件。 */
    @PostMapping("/{id}/submit")
    @RequiresRole({"DOCTOR"})
    public Result<Void> submit(@PathVariable("id") Long id) {
        encounterService.submitOrders(id);
        return Result.ok();
    }

    /** 被驳回处方重新送审。 */
    @PostMapping("/{id}/orders/{orderId}/resubmit")
    @RequiresRole({"DOCTOR"})
    public Result<Void> resubmitOrder(@PathVariable("id") Long id,
                                      @PathVariable("orderId") Long orderId) {
        encounterService.resubmitRejectedOrder(id, orderId);
        return Result.ok();
    }

    /** 作废草稿/被驳回医嘱。 */
    @PostMapping("/{id}/orders/{orderId}/cancel")
    @RequiresRole({"DOCTOR"})
    public Result<Void> cancelOrder(@PathVariable("id") Long id,
                                    @PathVariable("orderId") Long orderId) {
        encounterService.cancelOrder(id, orderId);
        return Result.ok();
    }

    @GetMapping("/{id}")
    public Result<EncounterVo> get(@PathVariable("id") Long id) {
        return Result.ok(EncounterVo.from(encounterService.getEncounter(id)));
    }

    @GetMapping("/{id}/orders")
    public Result<List<OrderVo>> orders(@PathVariable("id") Long id) {
        return Result.ok(encounterService.listOrders(id).stream().map(OrderVo::from).toList());
    }

    private CurrentUser currentDoctor() {
        return UserContext.get()
                .orElseThrow(() -> BusinessException.of(ResultCode.UNAUTHORIZED));
    }
}
