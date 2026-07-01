package com.lrj.his.emr.fhir;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Component;

/**
 * FHIR 资源 → JSON 字符串。IParser 非线程安全,每次调用新建(FhirContext 才是重对象、单例)。
 */
@Component
public class FhirJsonSerializer {

    private final FhirContext fhirContext;

    public FhirJsonSerializer(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }

    public String toJson(IBaseResource resource) {
        return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(resource);
    }
}
