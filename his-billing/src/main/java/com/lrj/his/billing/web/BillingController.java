package com.lrj.his.billing.web;

import com.lrj.his.billing.service.BillingService;
import com.lrj.his.common.web.Result;
import com.lrj.his.security.rbac.RequiresRole;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/billing/invoices")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/{id}")
    public Result<InvoiceVo> get(@PathVariable("id") Long id) {
        return Result.ok(InvoiceVo.from(billingService.getInvoice(id)));
    }

    @GetMapping("/by-encounter/{encounterId}")
    public Result<InvoiceVo> getByEncounter(@PathVariable("encounterId") Long encounterId) {
        return Result.ok(InvoiceVo.from(billingService.getByEncounter(encounterId)));
    }

    /** 缴费:收费员或管理员。 */
    @PostMapping("/{id}/pay")
    @RequiresRole({"CASHIER", "ADMIN"})
    public Result<InvoiceVo> pay(@PathVariable("id") Long id, @Valid @RequestBody PayRequest request) {
        return Result.ok(InvoiceVo.from(billingService.pay(id, request.payMethod())));
    }
}
