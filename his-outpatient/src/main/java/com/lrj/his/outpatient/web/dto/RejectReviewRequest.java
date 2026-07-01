package com.lrj.his.outpatient.web.dto;

import jakarta.validation.constraints.NotBlank;

/** 审方驳回请求:必须填写驳回意见。 */
public record RejectReviewRequest(@NotBlank String opinion) {
}
