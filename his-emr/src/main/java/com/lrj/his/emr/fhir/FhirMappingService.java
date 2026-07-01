package com.lrj.his.emr.fhir;

import com.lrj.his.api.mdm.dto.PatientDto;
import com.lrj.his.api.outpatient.dto.EncounterDto;
import com.lrj.his.api.outpatient.dto.OrderDto;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 领域数据 → FHIR R4 资源 映射。集中处理编码系统、引用关系,产出标准资源。
 */
@Service
public class FhirMappingService {

    private static final String SYS_EMPI = "urn:his:empi";
    private static final String SYS_ICD10 = "http://hl7.org/fhir/sid/icd-10";
    private static final String SYS_DRUG = "urn:his:drug";
    private static final String SYS_SERVICE = "urn:his:service-item";

    public Patient toPatient(PatientDto dto) {
        Patient p = new Patient();
        p.setId(String.valueOf(dto.id()));
        p.addIdentifier().setSystem(SYS_EMPI).setValue(dto.empiNo());
        p.addName().setText(dto.name());
        p.setGender(mapGender(dto.gender()));
        if (dto.birthDate() != null) {
            p.setBirthDate(toDate(dto.birthDate()));
        }
        return p;
    }

    public Encounter toEncounter(EncounterDto dto) {
        Encounter e = new Encounter();
        e.setId(String.valueOf(dto.id()));
        e.setStatus(Encounter.EncounterStatus.FINISHED);
        e.getClass_().setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode")
                .setCode("AMB").setDisplay("ambulatory");
        e.setSubject(new Reference("Patient/" + dto.patientId()));
        if (dto.doctorName() != null) {
            e.addParticipant().setIndividual(new Reference().setDisplay(dto.doctorName()));
        }
        e.addReasonCode().setText(dto.chiefComplaint());
        return e;
    }

    public Condition toDiagnosis(EncounterDto dto) {
        Condition c = new Condition();
        c.setId("diagnosis-" + dto.id());
        c.getCode().addCoding().setSystem(SYS_ICD10)
                .setCode(dto.diagnosisCode()).setDisplay(dto.diagnosisName());
        c.setSubject(new Reference("Patient/" + dto.patientId()));
        c.setEncounter(new Reference("Encounter/" + dto.id()));
        return c;
    }

    /** 药品 → MedicationRequest;检查/治疗 → ServiceRequest。 */
    public Resource toOrderResource(OrderDto order, Long patientId, Long encounterId) {
        Reference subject = new Reference("Patient/" + patientId);
        Reference encounter = new Reference("Encounter/" + encounterId);
        if ("DRUG".equals(order.orderType())) {
            MedicationRequest mr = new MedicationRequest();
            mr.setId("order-" + order.id());
            mr.setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE);
            mr.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
            mr.setMedication(coding(SYS_DRUG, order.itemCode(), order.itemName()));
            mr.setSubject(subject);
            mr.setEncounter(encounter);
            return mr;
        }
        ServiceRequest sr = new ServiceRequest();
        sr.setId("order-" + order.id());
        sr.setStatus(ServiceRequest.ServiceRequestStatus.ACTIVE);
        sr.setIntent(ServiceRequest.ServiceRequestIntent.ORDER);
        sr.setCode(coding(SYS_SERVICE, order.itemCode(), order.itemName()));
        sr.setSubject(subject);
        sr.setEncounter(encounter);
        return sr;
    }

    /**
     * 组装门诊病历文档(FHIR Bundle type=document):
     * Composition 摘要 + Patient + Encounter + Condition(诊断) + 各医嘱资源。
     */
    public Bundle toDocumentBundle(PatientDto patientDto, EncounterDto encDto, List<OrderDto> orders) {
        Patient patient = toPatient(patientDto);
        Encounter encounter = toEncounter(encDto);
        Condition diagnosis = toDiagnosis(encDto);
        List<Resource> orderResources = new ArrayList<>();
        for (OrderDto o : orders) {
            orderResources.add(toOrderResource(o, encDto.patientId(), encDto.id()));
        }

        Composition composition = new Composition();
        composition.setId("composition-" + encDto.id());
        composition.setStatus(Composition.CompositionStatus.FINAL);
        composition.getType().addCoding().setSystem("http://loinc.org")
                .setCode("11488-4").setDisplay("Consultation note");
        composition.setSubject(new Reference("Patient/" + encDto.patientId()));
        composition.setEncounter(new Reference("Encounter/" + encDto.id()));
        composition.setDate(new Date());
        composition.setTitle("门诊病历");
        if (encDto.doctorName() != null) {
            composition.addAuthor().setDisplay(encDto.doctorName());
        }
        Composition.SectionComponent dxSection = composition.addSection();
        dxSection.setTitle("诊断");
        dxSection.addEntry(new Reference("Condition/" + diagnosis.getId()));
        Composition.SectionComponent orderSection = composition.addSection();
        orderSection.setTitle("医嘱");
        orderResources.forEach(r -> orderSection.addEntry(
                new Reference(r.fhirType() + "/" + r.getIdElement().getIdPart())));

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.DOCUMENT);
        bundle.addEntry().setResource(composition);
        bundle.addEntry().setResource(patient);
        bundle.addEntry().setResource(encounter);
        bundle.addEntry().setResource(diagnosis);
        orderResources.forEach(r -> bundle.addEntry().setResource(r));
        return bundle;
    }

    private CodeableConcept coding(String system, String code, String display) {
        CodeableConcept cc = new CodeableConcept();
        cc.addCoding().setSystem(system).setCode(code).setDisplay(display);
        cc.setText(display);
        return cc;
    }

    private Enumerations.AdministrativeGender mapGender(String gender) {
        if (gender == null) {
            return Enumerations.AdministrativeGender.UNKNOWN;
        }
        return switch (gender) {
            case "MALE" -> Enumerations.AdministrativeGender.MALE;
            case "FEMALE" -> Enumerations.AdministrativeGender.FEMALE;
            default -> Enumerations.AdministrativeGender.UNKNOWN;
        };
    }

    private Date toDate(LocalDate d) {
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
