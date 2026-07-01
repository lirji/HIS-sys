package com.lrj.his.billing.web;

import com.lrj.his.billing.service.RefundService;
import com.lrj.his.common.context.CurrentUser;
import com.lrj.his.common.context.UserContext;
import com.lrj.his.common.exception.BusinessException;
import com.lrj.his.common.exception.ResultCode;
import com.lrj.his.common.web.Result;
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
 * 退费审批流入口。收费员发起、审批人(ADMIN)审批,身份取自登录上下文。
 */
@RestController
@RequestMapping("/billing")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    /** 收费员对已缴费账单发起退费申请。 */
    @PostMapping("/invoices/{id}/refund-requests")
    @RequiresRole({"CASHIER"})
    public Result<RefundRequestVo> request(@PathVariable("id") Long invoiceId,
                                           @Valid @RequestBody RefundRequestCreate body) {
        return Result.ok(RefundRequestVo.from(
                refundService.request(invoiceId, body.reason(), currentUser())));
    }

    @GetMapping("/refund-requests/pending")
    @RequiresRole({"ADMIN"})
    public Result<List<RefundRequestVo>> pending() {
        return Result.ok(refundService.listPending().stream().map(RefundRequestVo::from).toList());
    }

    @PostMapping("/refund-requests/{id}/approve")
    @RequiresRole({"ADMIN"})
    public Result<RefundRequestVo> approve(@PathVariable("id") Long id) {
        return Result.ok(RefundRequestVo.from(refundService.approve(id, currentUser())));
    }

    @PostMapping("/refund-requests/{id}/reject")
    @RequiresRole({"ADMIN"})
    public Result<RefundRequestVo> reject(@PathVariable("id") Long id,
                                          @Valid @RequestBody RefundRejectRequest body) {
        return Result.ok(RefundRequestVo.from(refundService.reject(id, body.opinion(), currentUser())));
    }

    private CurrentUser currentUser() {
        return UserContext.get()
                .orElseThrow(() -> BusinessException.of(ResultCode.UNAUTHORIZED));
    }
}
