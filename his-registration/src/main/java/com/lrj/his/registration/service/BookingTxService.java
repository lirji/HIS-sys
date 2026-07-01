package com.lrj.his.registration.service;

import com.lrj.his.common.exception.BusinessException;
import com.lrj.his.common.exception.ResultCode;
import com.lrj.his.registration.domain.Appointment;
import com.lrj.his.registration.domain.AppointmentRepository;
import com.lrj.his.registration.domain.Schedule;
import com.lrj.his.registration.domain.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 挂号落库的事务单元。独立成 Bean,确保 @Transactional 经代理生效
 * (避免 RegistrationService 内部自调用导致事务失效)。
 */
@Service
public class BookingTxService {

    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;

    public BookingTxService(ScheduleRepository scheduleRepository,
                            AppointmentRepository appointmentRepository) {
        this.scheduleRepository = scheduleRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public Appointment persist(Long scheduleId, Long patientId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> BusinessException.of(ResultCode.SCHEDULE_NOT_FOUND));
        schedule.takeSlot();                    // @Version 乐观锁兜底
        scheduleRepository.save(schedule);
        Appointment appointment = Appointment.book(schedule, patientId, schedule.currentSerialNo());
        return appointmentRepository.save(appointment);
    }
}
