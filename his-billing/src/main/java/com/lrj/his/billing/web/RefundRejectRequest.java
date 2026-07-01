package com.lrj.his.billing.web;

import jakarta.validation.constraints.NotBlank;

/** 驳回退费申请:必须填写驳回意见。 */
public record RefundRejectRequest(@NotBlank String opinion) {
}
