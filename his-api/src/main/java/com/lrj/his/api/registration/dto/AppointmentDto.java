package com.lrj.his.api.registration.dto;

import java.io.Serializable;

/**
 * 挂号记录对外视图。门诊接诊时据 appointmentId 取患者/科室信息。
 */
public record AppointmentDto(
        Long id,
        Long scheduleId,
        Long patientId,
        String deptCode,
        String doctorName,
        int serialNo,
        String status) implements Serializable {
}
