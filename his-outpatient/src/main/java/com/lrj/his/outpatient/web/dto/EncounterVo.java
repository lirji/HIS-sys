package com.lrj.his.outpatient.web.dto;

import com.lrj.his.outpatient.domain.Encounter;

public record EncounterVo(
        Long id,
        Long appointmentId,
        Long patientId,
        String deptCode,
        Long doctorId,
        String doctorName,
        String chiefComplaint,
        String diagnosisCode,
        String diagnosisName,
        String status) {

    public static EncounterVo from(Encounter e) {
        return new EncounterVo(e.getId(), e.getAppointmentId(), e.getPatientId(), e.getDeptCode(),
                e.getDoctorId(), e.getDoctorName(), e.getChiefComplaint(),
                e.getDiagnosisCode(), e.getDiagnosisName(), e.getStatus().name());
    }
}
