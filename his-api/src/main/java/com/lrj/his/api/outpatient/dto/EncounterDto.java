package com.lrj.his.api.outpatient.dto;

import java.io.Serializable;

/**
 * 就诊对外视图。字段与 his-outpatient 的 EncounterVo 对齐,供 Feign 反序列化。
 */
public record EncounterDto(
        Long id,
        Long appointmentId,
        Long patientId,
        String deptCode,
        Long doctorId,
        String doctorName,
        String chiefComplaint,
        String diagnosisCode,
        String diagnosisName,
        String status) implements Serializable {
}
