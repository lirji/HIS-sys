package com.lrj.his.outpatient.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RecordDiagnosisRequest(
        String chiefComplaint,
        @NotBlank String diagnosisCode,
        @NotBlank String diagnosisName) {
}
