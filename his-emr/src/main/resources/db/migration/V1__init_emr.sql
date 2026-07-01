-- 病历服务初始 schema: 临床病历文档 (FHIR 资源以 JSONB 落库)

CREATE TABLE clinical_document (
    id           BIGSERIAL PRIMARY KEY,
    encounter_id BIGINT      NOT NULL UNIQUE,
    patient_id   BIGINT      NOT NULL,
    doc_type     VARCHAR(32) NOT NULL,
    fhir_type    VARCHAR(32) NOT NULL,
    content      JSONB       NOT NULL,
    created_by   VARCHAR(64),
    created_at   TIMESTAMPTZ,
    updated_by   VARCHAR(64),
    updated_at   TIMESTAMPTZ,
    version      BIGINT      NOT NULL DEFAULT 0
);
CREATE INDEX idx_clinical_document_patient ON clinical_document (patient_id);
