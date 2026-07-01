package com.lrj.his.mdm.web;

import com.lrj.his.mdm.domain.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreatePatientRequest(
        @NotBlank(message = "姓名不能为空") String name,
        @NotNull(message = "性别不能为空") Gender gender,
        LocalDate birthDate,
        @NotBlank(message = "身份证不能为空") String idCard,
        String phone,
        String address) {
}
