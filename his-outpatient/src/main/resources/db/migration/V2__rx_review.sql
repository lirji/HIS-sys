-- Step 6:药师审方审批流。med_order.state 已是 VARCHAR(16),新增枚举值无需改表。

-- 计费推迟标志:就诊无待审医嘱时才发计费事件,防重复建单。
ALTER TABLE encounter ADD COLUMN billed BOOLEAN NOT NULL DEFAULT FALSE;

-- 审方留痕:一条记录对应一次就诊的一次审核动作。
CREATE TABLE prescription_review (
    id              BIGSERIAL PRIMARY KEY,
    encounter_id    BIGINT       NOT NULL,
    pharmacist_id   BIGINT,
    pharmacist_name VARCHAR(64),
    result          VARCHAR(16)  NOT NULL,   -- PASS / REJECT
    opinion         VARCHAR(500),
    reviewed_at     TIMESTAMPTZ  NOT NULL,
    created_by      VARCHAR(64),
    created_at      TIMESTAMPTZ,
    updated_by      VARCHAR(64),
    updated_at      TIMESTAMPTZ,
    version         BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_rx_review_encounter ON prescription_review (encounter_id);
