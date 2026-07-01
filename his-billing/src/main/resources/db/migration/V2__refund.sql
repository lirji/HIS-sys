-- Step 6:退费审批流。invoice.status 已是 VARCHAR(16),新增 REFUNDED 枚举值无需改表。

ALTER TABLE invoice ADD COLUMN refunded_at TIMESTAMPTZ;

CREATE TABLE refund_request (
    id                BIGSERIAL PRIMARY KEY,
    invoice_id        BIGINT        NOT NULL,
    encounter_id      BIGINT        NOT NULL,
    amount            NUMERIC(12,2) NOT NULL,
    reason            VARCHAR(500),
    status            VARCHAR(16)   NOT NULL,   -- REQUESTED / APPROVED / REJECTED
    opinion           VARCHAR(500),
    applicant_user_id BIGINT,
    applicant_name    VARCHAR(64),
    reviewer_name     VARCHAR(64),
    reviewed_at       TIMESTAMPTZ,
    created_by        VARCHAR(64),
    created_at        TIMESTAMPTZ,
    updated_by        VARCHAR(64),
    updated_at        TIMESTAMPTZ,
    version           BIGINT        NOT NULL DEFAULT 0
);
CREATE INDEX idx_refund_invoice ON refund_request (invoice_id);
CREATE INDEX idx_refund_status ON refund_request (status);
