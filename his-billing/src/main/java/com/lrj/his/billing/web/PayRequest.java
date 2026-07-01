package com.lrj.his.billing.web;

import jakarta.validation.constraints.NotBlank;

public record PayRequest(@NotBlank String payMethod) {
}
