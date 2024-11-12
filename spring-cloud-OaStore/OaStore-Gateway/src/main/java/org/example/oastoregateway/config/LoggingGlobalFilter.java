package org.example.oastoregateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Order(0)  // 过滤器的执行顺序，数字越小越优先
public class LoggingGlobalFilter implements GlobalFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        logger.info("Request URI: {}", request.getURI());
        logger.info("Request Method: {}", request.getMethod());

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            logger.info("Response Status Code: {}", exchange.getResponse().getStatusCode());
        }));
    }
}

