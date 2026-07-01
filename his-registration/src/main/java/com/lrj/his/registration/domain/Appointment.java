package com.lrj.his.registration.domain;

import com.lrj.his.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * 挂号记录。一次成功挂号占用一个号源,serialNo 为当日该排班的就诊顺序号。
 */
@Entity
@Table(name = "appointment")
public class Appointment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "dept_code", nullable = false, length = 32)
    private String deptCode;

    @Column(name = "doctor_name", nullable = false, length = 64)
    private String doctorName;

    @Column(name = "serial_no", nullable = false)
    private int serialNo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AppointmentStatus status;

    protected Appointment() {
    }

    public static Appointment book(Schedule schedule, Long patientId, int serialNo) {
        Appointment a = new Appointment();
        a.scheduleId = schedule.getId();
        a.patientId = patientId;
        a.deptCode = schedule.getDeptCode();
        a.doctorName = schedule.getDoctorName();
        a.serialNo = serialNo;
        a.fee = schedule.getFee();
        a.status = AppointmentStatus.BOOKED;
        return a;
    }

    public void markVisited() {
        this.status = AppointmentStatus.VISITED;
    }

    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public int getSerialNo() {
        return serialNo;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public AppointmentStatus getStatus() {
        return status;
    }
}
