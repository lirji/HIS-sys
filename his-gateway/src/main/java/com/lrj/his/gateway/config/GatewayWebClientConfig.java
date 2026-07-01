package com.lrj.his.gateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 负载均衡 WebClient(供身份过滤器回查 lb://his-auth)。idp 与 dual 两种模式共用。
 */
@Configuration
@Profile("idp | dual")
public class GatewayWebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}
