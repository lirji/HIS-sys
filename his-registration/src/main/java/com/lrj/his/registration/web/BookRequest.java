package com.lrj.his.registration.web;

import jakarta.validation.constraints.NotNull;

public record BookRequest(
        @NotNull Long scheduleId,
        @NotNull Long patientId) {
}
