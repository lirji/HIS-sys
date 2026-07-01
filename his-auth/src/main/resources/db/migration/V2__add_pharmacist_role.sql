-- 新增药师角色(处方审核审批流)。账号由 DataInitializer 在用户表为空时播种。
INSERT INTO sys_role (code, name) VALUES ('PHARMACIST', '药师');
