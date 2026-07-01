package com.lrj.his.registration.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByScheduleDateAndDeptCode(LocalDate scheduleDate, String deptCode);
}
