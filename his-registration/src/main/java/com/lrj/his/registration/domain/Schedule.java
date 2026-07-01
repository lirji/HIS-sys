package com.lrj.his.registration.domain;

import com.lrj.his.common.audit.AuditableEntity;
import com.lrj.his.common.exception.BusinessException;
import com.lrj.his.common.exception.ResultCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 排班 / 号源池。availableSlots 是 DB 侧权威库存,配合 @Version 乐观锁兜底;
 * 高并发抢号的第一道防线在 Redis(见 SlotLockService),DB 做最终一致校验。
 */
@Entity
@Table(name = "schedule")
public class Schedule extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "doctor_name", nullable = false, length = 64)
    private String doctorName;

    @Column(name = "dept_code", nullable = false, length = 32)
    private String deptCode;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    private Period period;

    @Column(name = "total_slots", nullable = false)
    private int totalSlots;

    @Column(name = "available_slots", nullable = false)
    private int availableSlots;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fee;

    protected Schedule() {
    }

    public static Schedule open(Long doctorId, String doctorName, String deptCode,
                                LocalDate date, Period period, int totalSlots, BigDecimal fee) {
        Schedule s = new Schedule();
        s.doctorId = doctorId;
        s.doctorName = doctorName;
        s.deptCode = deptCode;
        s.scheduleDate = date;
        s.period = period;
        s.totalSlots = totalSlots;
        s.availableSlots = totalSlots;
        s.fee = fee;
        return s;
    }

    /** DB 侧扣减库存。Redis 已先行拦截,此处兜底防越卖。 */
    public void takeSlot() {
        if (availableSlots <= 0) {
            throw BusinessException.of(ResultCode.SLOT_SOLD_OUT);
        }
        availableSlots--;
    }

    public void releaseSlot() {
        if (availableSlots < totalSlots) {
            availableSlots++;
        }
    }

    /** 已挂号序号 = 总号源 - 剩余(扣减后调用) */
    public int currentSerialNo() {
        return totalSlots - availableSlots;
    }

    public Long getId() {
        return id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public LocalDate getScheduleDate() {
        return scheduleDate;
    }

    public Period getPeriod() {
        return period;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public BigDecimal getFee() {
        return fee;
    }
}
