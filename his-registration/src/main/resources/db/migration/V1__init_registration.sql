-- 挂号服务初始 schema: 排班 / 挂号

CREATE TABLE schedule (
    id              BIGSERIAL PRIMARY KEY,
    doctor_id       BIGINT       NOT NULL,
    doctor_name     VARCHAR(64)  NOT NULL,
    dept_code       VARCHAR(32)  NOT NULL,
    schedule_date   DATE         NOT NULL,
    period          VARCHAR(4)   NOT NULL,
    total_slots     INT          NOT NULL,
    available_slots INT          NOT NULL,
    fee             NUMERIC(10,2) NOT NULL,
    created_by      VARCHAR(64),
    created_at      TIMESTAMPTZ,
    updated_by      VARCHAR(64),
    updated_at      TIMESTAMPTZ,
    version         BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_schedule_date_dept ON schedule (schedule_date, dept_code);

CREATE TABLE appointment (
    id          BIGSERIAL PRIMARY KEY,
    schedule_id BIGINT       NOT NULL REFERENCES schedule (id),
    patient_id  BIGINT       NOT NULL,
    dept_code   VARCHAR(32)  NOT NULL,
    doctor_name VARCHAR(64)  NOT NULL,
    serial_no   INT          NOT NULL,
    fee         NUMERIC(10,2) NOT NULL,
    status      VARCHAR(16)  NOT NULL,
    created_by  VARCHAR(64),
    created_at  TIMESTAMPTZ,
    updated_by  VARCHAR(64),
    updated_at  TIMESTAMPTZ,
    version     BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_appointment_patient ON appointment (patient_id);
