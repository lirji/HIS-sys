package com.lrj.his.emr.domain;

import com.lrj.his.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 临床病历文档。content 存 FHIR 资源 JSON(PG JSONB),encounterId 唯一。
 * 一次接诊生成一份门诊病历文档(FHIR Bundle:Composition+Patient+Encounter+Condition+Order)。
 */
@Entity
@Table(name = "clinical_document")
public class ClinicalDocument extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "encounter_id", nullable = false, unique = true)
    private Long encounterId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "doc_type", nullable = false, length = 32)
    private String docType;

    @Column(name = "fhir_type", nullable = false, length = 32)
    private String fhirType;

    /** FHIR 资源 JSON,存 PG jsonb */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String content;

    protected ClinicalDocument() {
    }

    public static ClinicalDocument of(Long encounterId, Long patientId, String docType,
                                      String fhirType, String content) {
        ClinicalDocument d = new ClinicalDocument();
        d.encounterId = encounterId;
        d.patientId = patientId;
        d.docType = docType;
        d.fhirType = fhirType;
        d.content = content;
        return d;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public Long getEncounterId() {
        return encounterId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getDocType() {
        return docType;
    }

    public String getFhirType() {
        return fhirType;
    }

    public String getContent() {
        return content;
    }
}
