package com.lrj.his.idp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 身份提供方(IdP)。Spring Authorization Server 实现 OIDC 授权码+PKCE,签发 RS256 access_token;
 * 用户/口令仍以 his-auth 为单一事实来源(登录时经 Feign 委托核验)。
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.lrj.his.api")
public class HisIdpApplication {

    public static void main(String[] args) {
        SpringApplication.run(HisIdpApplication.class, args);
    }
}
