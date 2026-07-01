-- 认证服务初始 schema: 用户 / 角色 / 关联

CREATE TABLE sys_role (
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL
);

CREATE TABLE sys_user (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(64)  NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,
    real_name  VARCHAR(64)  NOT NULL,
    dept_id    BIGINT,
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by VARCHAR(64),
    created_at TIMESTAMPTZ,
    updated_by VARCHAR(64),
    updated_at TIMESTAMPTZ,
    version    BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL REFERENCES sys_user (id),
    role_id BIGINT NOT NULL REFERENCES sys_role (id),
    PRIMARY KEY (user_id, role_id)
);

-- 角色种子
INSERT INTO sys_role (code, name) VALUES
    ('ADMIN',  '系统管理员'),
    ('DOCTOR', '医生'),
    ('NURSE',  '护士'),
    ('CASHIER','收费员');
