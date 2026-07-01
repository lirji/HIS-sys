package com.lrj.his.mdm.service;

import com.lrj.his.api.mdm.dto.PatientDto;
import com.lrj.his.common.exception.BusinessException;
import com.lrj.his.common.exception.ResultCode;
import com.lrj.his.common.util.MaskUtil;
import com.lrj.his.mdm.domain.Patient;
import com.lrj.his.mdm.domain.PatientRepository;
import com.lrj.his.mdm.web.CreatePatientRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository repository;

    @PersistenceContext
    private EntityManager em;

    public PatientService(PatientRepository repository) {
        this.repository = repository;
    }

    /**
     * 登记患者(EMPI 去重核心):同身份证已存在则判为同一人,拒绝重复建档。
     * empiNo 用数据库序列生成,全院唯一稳定。
     */
    @Transactional
    public PatientDto register(CreatePatientRequest req) {
        if (req.idCard() != null && repository.existsByIdCard(req.idCard())) {
            throw BusinessException.of(ResultCode.PATIENT_DUPLICATED);
        }
        String empiNo = nextEmpiNo();
        Patient patient = Patient.register(empiNo, req.name(), req.gender(),
                req.birthDate(), req.idCard(), req.phone(), req.address());
        return toDto(repository.save(patient));
    }

    @Transactional(readOnly = true)
    public PatientDto getById(Long id) {
        Patient patient = repository.findById(id)
                .orElseThrow(() -> BusinessException.of(ResultCode.PATIENT_NOT_FOUND));
        return toDto(patient);
    }

    private String nextEmpiNo() {
        Object val = em.createNativeQuery("SELECT nextval('empi_no_seq')").getSingleResult();
        long seq = ((Number) val).longValue();
        return String.format("P%010d", seq);
    }

    /** 对外视图统一脱敏身份证/手机号。 */
    private PatientDto toDto(Patient p) {
        return new PatientDto(
                p.getId(),
                p.getEmpiNo(),
                p.getName(),
                p.getGender().name(),
                p.getBirthDate(),
                MaskUtil.idCard(p.getIdCard()),
                MaskUtil.phone(p.getPhone()));
    }
}
