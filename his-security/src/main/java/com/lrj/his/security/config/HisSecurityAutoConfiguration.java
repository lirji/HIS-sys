package com.lrj.his.security.config;

import com.lrj.his.common.exception.GlobalExceptionHandler;
import com.lrj.his.security.rbac.RoleCheckAspect;
import com.lrj.his.security.web.UserContextInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * his-security 自动装配。任何引入本模块且是 Web MVC 应用的服务,
 * 自动获得 UserContext 装配 + RBAC 切面,无需手写配置。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(WebMvcConfigurer.class)
public class HisSecurityAutoConfiguration {

    @Bean
    public UserContextInterceptor userContextInterceptor() {
        return new UserContextInterceptor();
    }

    @Bean
    public RoleCheckAspect roleCheckAspect() {
        return new RoleCheckAspect();
    }

    /** 统一异常处理,所有依赖本模块的 web 服务自动生效 */
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public WebMvcConfigurer userContextWebMvcConfigurer(UserContextInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor).addPathPatterns("/**");
            }
        };
    }
}
