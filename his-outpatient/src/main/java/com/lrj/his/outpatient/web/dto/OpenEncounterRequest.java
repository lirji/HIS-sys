package com.lrj.his.outpatient.web.dto;

import jakarta.validation.constraints.NotNull;

public record OpenEncounterRequest(@NotNull Long appointmentId) {
}
