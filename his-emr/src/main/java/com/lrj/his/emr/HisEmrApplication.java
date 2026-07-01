package com.lrj.his.emr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.lrj.his.api")
public class HisEmrApplication {
    public static void main(String[] args) {
        SpringApplication.run(HisEmrApplication.class, args);
    }
}
