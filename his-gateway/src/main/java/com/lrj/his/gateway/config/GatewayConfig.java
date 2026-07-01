package com.lrj.his.gateway.config;

import com.lrj.his.common.jwt.JwtTokenService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GatewayJwtProperties.class)
public class GatewayConfig {

    @Bean
    public JwtTokenService jwtTokenService(GatewayJwtProperties props) {
        return new JwtTokenService(props.getSecret(), props.getTtlSeconds(), props.getIssuer());
    }
}
