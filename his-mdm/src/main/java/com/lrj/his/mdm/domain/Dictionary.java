package com.lrj.his.mdm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 通用字典。type 区分类别(DEPARTMENT/ICD10/DRUG...),(type,code) 唯一。
 * 全院共享的科室、诊断、药品目录都落这张表,避免散落各服务。
 */
@Entity
@Table(name = "dictionary",
        uniqueConstraints = @UniqueConstraint(name = "uk_dict_type_code", columnNames = {"type", "code"}))
public class Dictionary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String type;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "parent_code", length = 64)
    private String parentCode;

    @Column(name = "sort_no")
    private Integer sortNo;

    @Column(nullable = false)
    private boolean enabled = true;

    protected Dictionary() {
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getParentCode() {
        return parentCode;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
