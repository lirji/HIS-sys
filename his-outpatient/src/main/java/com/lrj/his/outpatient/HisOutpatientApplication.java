package com.lrj.his.outpatient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.lrj.his.api")
public class HisOutpatientApplication {
    public static void main(String[] args) {
        SpringApplication.run(HisOutpatientApplication.class, args);
    }
}
