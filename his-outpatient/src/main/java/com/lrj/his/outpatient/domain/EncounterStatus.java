package com.lrj.his.outpatient.domain;

public enum EncounterStatus {
    OPEN,       // 接诊中(可开/改医嘱)
    SUBMITTED,  // 已提交医嘱(待缴费)
    CLOSED      // 已结束
}
