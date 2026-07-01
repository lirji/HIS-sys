package com.lrj.his.registration.web;

import com.lrj.his.registration.domain.Period;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OpenScheduleRequest(
        @NotNull Long doctorId,
        @NotBlank String doctorName,
        @NotBlank String deptCode,
        @NotNull LocalDate scheduleDate,
        @NotNull Period period,
        @Min(1) int totalSlots,
        @NotNull BigDecimal fee) {
}
