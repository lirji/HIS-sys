package com.lrj.his.emr.fhir;

import ca.uhn.fhir.context.FhirContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirConfig {

    /** FhirContext 创建昂贵且线程安全,全局单例。 */
    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR4();
    }
}
