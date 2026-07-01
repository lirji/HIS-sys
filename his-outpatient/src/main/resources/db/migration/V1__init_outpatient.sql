-- 门诊服务初始 schema: 就诊 + 医嘱

CREATE TABLE encounter (
    id              BIGSERIAL PRIMARY KEY,
    appointment_id  BIGINT       NOT NULL,
    patient_id      BIGINT       NOT NULL,
    dept_code       VARCHAR(32)  NOT NULL,
    doctor_id       BIGINT,
    doctor_name     VARCHAR(64),
    chief_complaint VARCHAR(500),
    diagnosis_code  VARCHAR(32),
    diagnosis_name  VARCHAR(128),
    status          VARCHAR(16)  NOT NULL,
    visit_time      TIMESTAMPTZ  NOT NULL,
    created_by      VARCHAR(64),
    created_at      TIMESTAMPTZ,
    updated_by      VARCHAR(64),
    updated_at      TIMESTAMPTZ,
    version         BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_encounter_patient ON encounter (patient_id);

-- order 是 SQL 保留字, 表名用 med_order
CREATE TABLE med_order (
    id           BIGSERIAL PRIMARY KEY,
    encounter_id BIGINT       NOT NULL REFERENCES encounter (id),
    patient_id   BIGINT       NOT NULL,
    order_type   VARCHAR(16)  NOT NULL,
    item_code    VARCHAR(64)  NOT NULL,
    item_name    VARCHAR(128) NOT NULL,
    quantity     INT          NOT NULL,
    unit_price   NUMERIC(10,2) NOT NULL,
    amount       NUMERIC(12,2) NOT NULL,
    state        VARCHAR(16)  NOT NULL,
    created_by   VARCHAR(64),
    created_at   TIMESTAMPTZ,
    updated_by   VARCHAR(64),
    updated_at   TIMESTAMPTZ,
    version      BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_order_encounter ON med_order (encounter_id);
