package com.lrj.his.outpatient.domain;

import com.lrj.his.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 就诊(门诊聚合根)。一次挂号对应一次接诊,承载主诉/诊断,并以它为单位提交医嘱。
 */
@Entity
@Table(name = "encounter")
public class Encounter extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "dept_code", nullable = false, length = 32)
    private String deptCode;

    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(name = "doctor_name", length = 64)
    private String doctorName;

    @Column(name = "chief_complaint", length = 500)
    private String chiefComplaint;

    @Column(name = "diagnosis_code", length = 32)
    private String diagnosisCode;

    @Column(name = "diagnosis_name", length = 128)
    private String diagnosisName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EncounterStatus status;

    @Column(name = "visit_time", nullable = false)
    private Instant visitTime;

    /** 是否已发计费事件(OrderPlaced)。审方场景下计费推迟到无待审医嘱时,用此标志防重复发单。 */
    @Column(nullable = false)
    private boolean billed = false;

    protected Encounter() {
    }

    public static Encounter open(Long appointmentId, Long patientId, String deptCode,
                                 Long doctorId, String doctorName) {
        Encounter e = new Encounter();
        e.appointmentId = appointmentId;
        e.patientId = patientId;
        e.deptCode = deptCode;
        e.doctorId = doctorId;
        e.doctorName = doctorName;
        e.status = EncounterStatus.OPEN;
        e.visitTime = Instant.now();
        return e;
    }

    public void recordDiagnosis(String chiefComplaint, String diagnosisCode, String diagnosisName) {
        this.chiefComplaint = chiefComplaint;
        this.diagnosisCode = diagnosisCode;
        this.diagnosisName = diagnosisName;
    }

    public void markSubmitted() {
        this.status = EncounterStatus.SUBMITTED;
    }

    public void markBilled() {
        this.billed = true;
    }

    public boolean isBilled() {
        return billed;
    }

    public boolean isOpen() {
        return status == EncounterStatus.OPEN;
    }

    public Long getId() {
        return id;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getChiefComplaint() {
        return chiefComplaint;
    }

    public String getDiagnosisCode() {
        return diagnosisCode;
    }

    public String getDiagnosisName() {
        return diagnosisName;
    }

    public EncounterStatus getStatus() {
        return status;
    }

    public Instant getVisitTime() {
        return visitTime;
    }
}
