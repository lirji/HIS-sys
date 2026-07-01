package com.lrj.his.registration.web;

import com.lrj.his.registration.domain.Appointment;

import java.math.BigDecimal;

public record AppointmentVo(
        Long id,
        Long scheduleId,
        Long patientId,
        String deptCode,
        String doctorName,
        int serialNo,
        BigDecimal fee,
        String status) {

    public static AppointmentVo from(Appointment a) {
        return new AppointmentVo(a.getId(), a.getScheduleId(), a.getPatientId(),
                a.getDeptCode(), a.getDoctorName(), a.getSerialNo(), a.getFee(), a.getStatus().name());
    }
}
