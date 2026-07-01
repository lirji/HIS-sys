-- 主数据服务初始 schema: 患者主索引 + 通用字典

-- EMPI 主索引号序列 (P + 10位)
CREATE SEQUENCE empi_no_seq START 1;

CREATE TABLE patient (
    id         BIGSERIAL PRIMARY KEY,
    empi_no    VARCHAR(20)  NOT NULL UNIQUE,
    name       VARCHAR(64)  NOT NULL,
    gender     VARCHAR(8)   NOT NULL,
    birth_date DATE,
    id_card    VARCHAR(18)  UNIQUE,
    phone      VARCHAR(11),
    address    VARCHAR(200),
    created_by VARCHAR(64),
    created_at TIMESTAMPTZ,
    updated_by VARCHAR(64),
    updated_at TIMESTAMPTZ,
    version    BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE dictionary (
    id          BIGSERIAL PRIMARY KEY,
    type        VARCHAR(32)  NOT NULL,
    code        VARCHAR(64)  NOT NULL,
    name        VARCHAR(128) NOT NULL,
    parent_code VARCHAR(64),
    sort_no     INT,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_dict_type_code UNIQUE (type, code)
);

-- 科室
INSERT INTO dictionary (type, code, name, sort_no) VALUES
    ('DEPARTMENT', '101', '内科', 1),
    ('DEPARTMENT', '102', '外科', 2),
    ('DEPARTMENT', '103', '儿科', 3),
    ('DEPARTMENT', '201', '收费处', 9);

-- ICD-10 诊断(示例)
INSERT INTO dictionary (type, code, name, sort_no) VALUES
    ('ICD10', 'J00',   '急性鼻咽炎(感冒)', 1),
    ('ICD10', 'I10',   '原发性高血压', 2),
    ('ICD10', 'E11',   '2型糖尿病', 3);

-- 药品目录(示例)
INSERT INTO dictionary (type, code, name, sort_no) VALUES
    ('DRUG', 'D001', '阿莫西林胶囊 0.25g', 1),
    ('DRUG', 'D002', '布洛芬缓释胶囊 0.3g', 2),
    ('DRUG', 'D003', '盐酸二甲双胍片 0.5g', 3);
