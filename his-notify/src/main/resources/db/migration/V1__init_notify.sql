-- 通知服务初始 schema: 通知/待办

CREATE TABLE notification (
    id                BIGSERIAL PRIMARY KEY,
    recipient_role    VARCHAR(32),    -- 角色待办(与 recipient_user_id 二选一)
    recipient_user_id BIGINT,         -- 个人消息
    type              VARCHAR(32)  NOT NULL,
    title             VARCHAR(128) NOT NULL,
    content           VARCHAR(1000),
    biz_type          VARCHAR(32),
    biz_id            BIGINT,
    read_flag         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by        VARCHAR(64),
    created_at        TIMESTAMPTZ,
    updated_by        VARCHAR(64),
    updated_at        TIMESTAMPTZ,
    version           BIGINT       NOT NULL DEFAULT 0
);
CREATE INDEX idx_notification_user ON notification (recipient_user_id);
CREATE INDEX idx_notification_role ON notification (recipient_role);
