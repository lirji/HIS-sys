package com.lrj.his.emr.service;

import com.lrj.his.api.mdm.PatientApi;
import com.lrj.his.api.mdm.dto.PatientDto;
import com.lrj.his.api.outpatient.OutpatientApi;
import com.lrj.his.api.outpatient.dto.EncounterDto;
import com.lrj.his.api.outpatient.dto.OrderDto;
import com.lrj.his.common.exception.BusinessException;
import com.lrj.his.common.exception.ResultCode;
import com.lrj.his.common.web.Result;
import com.lrj.his.emr.domain.ClinicalDocument;
import com.lrj.his.emr.domain.ClinicalDocumentRepository;
import com.lrj.his.emr.fhir.FhirJsonSerializer;
import com.lrj.his.emr.fhir.FhirMappingService;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 病历应用服务。从门诊/主数据取数,经 {@link FhirMappingService} 组装 FHIR 文档落库,
 * 并对外提供 FHIR R4 资源投影。
 */
@Service
public class EmrService {

    private static final String DOC_TYPE_OUTPATIENT = "OUTPATIENT_NOTE";

    private final PatientApi patientApi;
    private final OutpatientApi outpatientApi;
    private final ClinicalDocumentRepository repository;
    private final FhirMappingService mapping;
    private final FhirJsonSerializer serializer;

    public EmrService(PatientApi patientApi, OutpatientApi outpatientApi,
                      ClinicalDocumentRepository repository, FhirMappingService mapping,
                      FhirJsonSerializer serializer) {
        this.patientApi = patientApi;
        this.outpatientApi = outpatientApi;
        this.repository = repository;
        this.mapping = mapping;
        this.serializer = serializer;
    }

    /** 生成/刷新一次接诊的门诊病历文档(FHIR Bundle),按 encounterId 幂等 upsert。 */
    @Transactional
    public ClinicalDocument generate(Long encounterId) {
        EncounterDto encounter = unwrap(outpatientApi.getEncounter(encounterId));
        PatientDto patient = unwrap(patientApi.getById(encounter.patientId()));
        List<OrderDto> orders = unwrap(outpatientApi.listOrders(encounterId));

        Bundle bundle = mapping.toDocumentBundle(patient, encounter, orders);
        String content = serializer.toJson(bundle);

        ClinicalDocument document = repository.findByEncounterId(encounterId)
                .map(existing -> {
                    existing.updateContent(content);
                    return existing;
                })
                .orElseGet(() -> ClinicalDocument.of(encounterId, encounter.patientId(),
                        DOC_TYPE_OUTPATIENT, "Bundle", content));
        return repository.save(document);
    }

    /** 返回已落库的病历文档 FHIR JSON;不存在则 404。 */
    @Transactional(readOnly = true)
    public String getDocumentContent(Long encounterId) {
        return repository.findByEncounterId(encounterId)
                .map(ClinicalDocument::getContent)
                .orElseThrow(() -> BusinessException.of(ResultCode.DOCUMENT_NOT_FOUND));
    }

    /** 实时投影 FHIR Patient 资源。 */
    public String patientResource(Long patientId) {
        return serializer.toJson(mapping.toPatient(unwrap(patientApi.getById(patientId))));
    }

    /** 实时投影 FHIR Encounter 资源。 */
    public String encounterResource(Long encounterId) {
        return serializer.toJson(mapping.toEncounter(unwrap(outpatientApi.getEncounter(encounterId))));
    }

    private <T> T unwrap(Result<T> result) {
        if (result == null || !result.success() || result.data() == null) {
            throw BusinessException.of(ResultCode.REMOTE_CALL_ERROR,
                    result == null ? "下游服务无响应" : result.message());
        }
        return result.data();
    }
}
