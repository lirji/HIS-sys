package com.lrj.his.registration.domain;

public enum AppointmentStatus {
    /** 已挂号待就诊 */
    BOOKED,
    /** 已就诊(门诊接诊后置位) */
    VISITED,
    /** 已退号 */
    CANCELLED
}
