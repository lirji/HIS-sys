package com.lrj.his.mdm.domain;

import com.lrj.his.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * 患者主索引 (EMPI 聚合根)。empiNo 全院唯一,是跨服务引用患者的稳定标识。
 * 身份证唯一约束 + 服务层去重共同实现"同一个人只有一份主索引"。
 */
@Entity
@Table(name = "patient")
public class Patient extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empi_no", nullable = false, unique = true, length = 20)
    private String empiNo;

    @Column(nullable = false, length = 64)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private Gender gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "id_card", unique = true, length = 18)
    private String idCard;

    @Column(length = 11)
    private String phone;

    @Column(length = 200)
    private String address;

    protected Patient() {
    }

    /** 工厂:登记新患者。empiNo 由服务层用序列生成后传入。 */
    public static Patient register(String empiNo, String name, Gender gender,
                                   LocalDate birthDate, String idCard, String phone, String address) {
        Patient p = new Patient();
        p.empiNo = empiNo;
        p.name = name;
        p.gender = gender == null ? Gender.UNKNOWN : gender;
        p.birthDate = birthDate;
        p.idCard = idCard;
        p.phone = phone;
        p.address = address;
        return p;
    }

    public void updateContact(String phone, String address) {
        this.phone = phone;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public String getEmpiNo() {
        return empiNo;
    }

    public String getName() {
        return name;
    }

    public Gender getGender() {
        return gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getIdCard() {
        return idCard;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }
}
