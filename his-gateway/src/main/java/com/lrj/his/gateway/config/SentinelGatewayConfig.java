package com.lrj.his.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.lrj.his.common.exception.ResultCode;
import com.lrj.his.common.web.Result;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.HashSet;
import java.util.Set;

/**
 * 网关 Sentinel 限流。规则按路由 id(GatewayFlowRule 默认 resourceMode=ROUTE_ID)在启动期编程式加载,
 * 不依赖外部 Sentinel 控制台。超限时返回统一 {@link Result} JSON(HTTP 429)而非默认纯文本。
 *
 * <p>SentinelGatewayFilter 与 BlockExceptionHandler 由 spring-cloud-alibaba-sentinel-gateway 自动装配,
 * 本类只负责装载流控规则 + 定制阻断响应。
 */
@Configuration
public class SentinelGatewayConfig {

    @PostConstruct
    public void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        // 登录入口防爆破:10 QPS
        rules.add(new GatewayFlowRule("his-auth").setCount(10).setIntervalSec(1));
        // 挂号热点(号源抢占)整体入口护栏:20 QPS,与下游 Redis Lua 号源锁互补
        rules.add(new GatewayFlowRule("his-registration").setCount(20).setIntervalSec(1));
        GatewayRuleManager.loadRules(rules);

        GatewayCallbackManager.setBlockHandler(blockHandler());
    }

    private BlockRequestHandler blockHandler() {
        Result<Void> body = Result.fail(ResultCode.TOO_MANY_REQUESTS);
        return (exchange, t) -> ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body));
    }
}
