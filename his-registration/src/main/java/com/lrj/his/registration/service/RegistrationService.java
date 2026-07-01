package com.lrj.his.registration.service;

import com.lrj.his.api.mdm.PatientApi;
import com.lrj.his.api.mdm.dto.PatientDto;
import com.lrj.his.api.registration.dto.AppointmentDto;
import com.lrj.his.common.exception.BusinessException;
import com.lrj.his.common.exception.ResultCode;
import com.lrj.his.common.web.Result;
import com.lrj.his.registration.domain.Appointment;
import com.lrj.his.registration.domain.AppointmentRepository;
import com.lrj.his.registration.domain.AppointmentStatus;
import com.lrj.his.registration.domain.Schedule;
import com.lrj.his.registration.domain.ScheduleRepository;
import com.lrj.his.registration.web.BookRequest;
import com.lrj.his.registration.web.OpenScheduleRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final SlotLockService slotLockService;
    private final BookingTxService bookingTxService;
    private final PatientApi patientApi;

    public RegistrationService(ScheduleRepository scheduleRepository,
                               AppointmentRepository appointmentRepository,
                               SlotLockService slotLockService,
                               BookingTxService bookingTxService,
                               PatientApi patientApi) {
        this.scheduleRepository = scheduleRepository;
        this.appointmentRepository = appointmentRepository;
        this.slotLockService = slotLockService;
        this.bookingTxService = bookingTxService;
        this.patientApi = patientApi;
    }

    @Transactional
    public Schedule openSchedule(OpenScheduleRequest req) {
        Schedule schedule = Schedule.open(req.doctorId(), req.doctorName(), req.deptCode(),
                req.scheduleDate(), req.period(), req.totalSlots(), req.fee());
        return scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public List<Schedule> listSchedules(LocalDate date, String deptCode) {
        return scheduleRepository.findByScheduleDateAndDeptCode(date, deptCode);
    }

    /**
     * 挂号。流程:Feign 校验患者 → Redis 原子抢号 → DB 事务扣减+建单。
     * Redis 抢号成功但后续 DB 失败时归还号源(补偿),保证不漏号。
     */
    public Appointment book(BookRequest req) {
        verifyPatient(req.patientId());

        Schedule schedule = scheduleRepository.findById(req.scheduleId())
                .orElseThrow(() -> BusinessException.of(ResultCode.SCHEDULE_NOT_FOUND));

        if (appointmentRepository.existsByScheduleIdAndPatientIdAndStatus(
                req.scheduleId(), req.patientId(), AppointmentStatus.BOOKED)) {
            throw BusinessException.of(ResultCode.APPOINTMENT_DUPLICATED);
        }

        // 第一道防线:Redis 原子扣减,抗高并发超挂
        if (!slotLockService.tryAcquire(schedule.getId(), schedule.getTotalSlots())) {
            throw BusinessException.of(ResultCode.SLOT_SOLD_OUT);
        }
        try {
            return bookingTxService.persist(req.scheduleId(), req.patientId());
        } catch (RuntimeException ex) {
            slotLockService.release(schedule.getId()); // 补偿:归还号源
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public AppointmentDto getAppointment(Long id) {
        Appointment a = appointmentRepository.findById(id)
                .orElseThrow(() -> BusinessException.of(ResultCode.NOT_FOUND, "挂号记录不存在"));
        return new AppointmentDto(a.getId(), a.getScheduleId(), a.getPatientId(),
                a.getDeptCode(), a.getDoctorName(), a.getSerialNo(), a.getStatus().name());
    }

    /** 门诊接诊后回写"已就诊"。 */
    @Transactional
    public void markVisited(Long id) {
        Appointment a = appointmentRepository.findById(id)
                .orElseThrow(() -> BusinessException.of(ResultCode.NOT_FOUND, "挂号记录不存在"));
        a.markVisited();
        appointmentRepository.save(a);
    }

    private void verifyPatient(Long patientId) {
        try {
            Result<PatientDto> resp = patientApi.getById(patientId);
            if (resp == null || !resp.success() || resp.data() == null) {
                throw BusinessException.of(ResultCode.PATIENT_NOT_FOUND);
            }
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            log.warn("调用 his-mdm 校验患者失败 patientId={}", patientId, ex);
            throw BusinessException.of(ResultCode.PATIENT_NOT_FOUND, "无法校验患者(主数据服务不可用)");
        }
    }
}
