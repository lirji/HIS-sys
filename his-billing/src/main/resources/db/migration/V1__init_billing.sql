-- 收费服务初始 schema: 账单 + 明细

CREATE TABLE invoice (
    id           BIGSERIAL PRIMARY KEY,
    encounter_id BIGINT       NOT NULL UNIQUE,   -- 幂等键
    patient_id   BIGINT       NOT NULL,
    dept_code    VARCHAR(32)  NOT NULL,
    total_amount NUMERIC(12,2) NOT NULL,
    status       VARCHAR(16)  NOT NULL,
    pay_method   VARCHAR(16),
    paid_at      TIMESTAMPTZ,
    created_by   VARCHAR(64),
    created_at   TIMESTAMPTZ,
    updated_by   VARCHAR(64),
    updated_at   TIMESTAMPTZ,
    version      BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_invoice_patient ON invoice (patient_id);

CREATE TABLE invoice_item (
    id         BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT       NOT NULL REFERENCES invoice (id),
    order_id   BIGINT       NOT NULL,
    item_code  VARCHAR(64)  NOT NULL,
    item_name  VARCHAR(128) NOT NULL,
    quantity   INT          NOT NULL,
    amount     NUMERIC(12,2) NOT NULL
);
CREATE INDEX idx_invoice_item_invoice ON invoice_item (invoice_id);
