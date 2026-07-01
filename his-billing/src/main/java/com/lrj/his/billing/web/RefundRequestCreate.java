package com.lrj.his.billing.web;

import jakarta.validation.constraints.NotBlank;

/** 发起退费申请:必须填写退费原因。 */
public record RefundRequestCreate(@NotBlank String reason) {
}
