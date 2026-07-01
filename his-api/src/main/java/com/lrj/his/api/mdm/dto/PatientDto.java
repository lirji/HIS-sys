package com.lrj.his.api.mdm.dto;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 患者主索引对外视图。其他服务通过 patientId 引用患者,需要展示信息时取此 DTO。
 *
 * @param id        患者业务主键
 * @param empiNo    企业级主索引号 (EMPI) — 全院唯一
 * @param name      姓名
 * @param gender    性别 MALE/FEMALE/UNKNOWN
 * @param birthDate 出生日期
 * @param idCard    身份证号(掩码后)
 * @param phone     手机号(掩码后)
 */
public record PatientDto(
        Long id,
        String empiNo,
        String name,
        String gender,
        LocalDate birthDate,
        String idCard,
        String phone
) implements Serializable {
}
