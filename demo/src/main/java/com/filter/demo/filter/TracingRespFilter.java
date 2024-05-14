package com.filter.demo.filter;


import io.micrometer.tracing.Span;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.filter.demo.config.TracingConfig.*;

@Component
@Order(Integer.MAX_VALUE-1)
public class TracingRespFilter implements WebFilter {
    private static final String TRACE_ID_NAME = "X-B3-TraceId";
    private static final String SPAN_ID_NAME = "X-B3-SpanId";

    Logger log = LoggerFactory.getLogger(TracingRespFilter.class);

    @Override
    public @NotNull Mono<Void> filter(ServerWebExchange exchange, WebFilterChain webFilterChain) {
        Span span = exchange.getAttribute(Span.class.getName());
        if(span!=null){
            log.info(span.toString());
        } else {
            log.info("span is null");
        }

        exchange.getResponse().getHeaders().add(TRACE_ID_NAME, "test_traceID");
        exchange.getResponse().getHeaders().add(SPAN_ID_NAME, "test_spanID");

        exchange.getResponse().getHeaders().add(DD_TRACE_NAME, "test_dd.traceID");
        exchange.getResponse().getHeaders().add(APP_VERSION, "test_appVersion");

        exchange.getResponse().getHeaders().add(MDC_USER_NAME, "This_is_user_id");
        return webFilterChain.filter(exchange);
    }
}
