package com.lrj.his.outpatient.web.dto;

import com.lrj.his.outpatient.domain.OrderType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AddOrderRequest(
        @NotNull OrderType orderType,
        @NotBlank String itemCode,
        @NotBlank String itemName,
        @Min(1) int quantity,
        @NotNull BigDecimal unitPrice) {
}
