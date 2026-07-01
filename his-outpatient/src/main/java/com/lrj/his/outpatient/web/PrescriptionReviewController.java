package com.lrj.his.outpatient.web;

import com.lrj.his.common.context.CurrentUser;
import com.lrj.his.common.context.UserContext;
import com.lrj.his.common.exception.BusinessException;
import com.lrj.his.common.exception.ResultCode;
import com.lrj.his.common.web.Result;
import com.lrj.his.outpatient.service.PrescriptionReviewService;
import com.lrj.his.outpatient.web.dto.PendingReviewVo;
import com.lrj.his.outpatient.web.dto.RejectReviewRequest;
import com.lrj.his.security.rbac.RequiresRole;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 药师审方工作台。仅药师角色,审方人身份取自登录上下文(网关透传)。
 */
@RestController
@RequestMapping("/outpatient/reviews")
public class PrescriptionReviewController {

    private final PrescriptionReviewService reviewService;

    public PrescriptionReviewController(PrescriptionReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/pending")
    @RequiresRole({"PHARMACIST"})
    public Result<List<PendingReviewVo>> pending() {
        return Result.ok(reviewService.listPending());
    }

    @PostMapping("/{encounterId}/pass")
    @RequiresRole({"PHARMACIST"})
    public Result<Void> pass(@PathVariable("encounterId") Long encounterId) {
        reviewService.pass(encounterId, currentPharmacist());
        return Result.ok();
    }

    @PostMapping("/{encounterId}/reject")
    @RequiresRole({"PHARMACIST"})
    public Result<Void> reject(@PathVariable("encounterId") Long encounterId,
                               @Valid @RequestBody RejectReviewRequest request) {
        reviewService.reject(encounterId, request.opinion(), currentPharmacist());
        return Result.ok();
    }

    private CurrentUser currentPharmacist() {
        return UserContext.get()
                .orElseThrow(() -> BusinessException.of(ResultCode.UNAUTHORIZED));
    }
}
