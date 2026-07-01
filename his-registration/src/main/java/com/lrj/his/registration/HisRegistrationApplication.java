package com.lrj.his.registration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.lrj.his.api")
public class HisRegistrationApplication {
    public static void main(String[] args) {
        SpringApplication.run(HisRegistrationApplication.class, args);
    }
}
