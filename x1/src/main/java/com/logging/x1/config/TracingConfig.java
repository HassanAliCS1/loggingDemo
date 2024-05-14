package com.logging.x1.config;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.contextpropagation.ObservationAwareSpanThreadLocalAccessor;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.WebFilter;
import org.zalando.logbook.*;
import org.zalando.logbook.core.*;
import org.zalando.logbook.json.JsonHttpLogFormatter;
import org.zalando.logbook.logstash.LogstashLogbackSink;
import org.zalando.logbook.netty.LogbookServerHandler;
import reactor.netty.Metrics;

import java.util.Optional;
import java.util.function.Function;

import static org.slf4j.event.Level.DEBUG;

@Configuration
@Slf4j
public class TracingConfig {

    private final Tracer tracer;
    private final ObservationRegistry observationRegistry;
    public static final String MDC_USER_NAME = "mdcUser";
    public static final String DD_TRACE_NAME = "dd.trace_id";
    public static final String APP_VERSION = "app-version";

    public TracingConfig(Tracer tracer, ObservationRegistry observationRegistry) {
        this.tracer = tracer;
        this.observationRegistry = observationRegistry;
    }

    @Bean
    public ContextSnapshotFactory contextSnapshotFactory() {
        return ContextSnapshotFactory.builder().build();
    }

    @Bean
    public CorrelationId correlationId() {
        return request -> Optional.ofNullable(tracer.currentSpan())
                .map(span -> span.context().traceId())
                .orElseGet(() -> new DefaultCorrelationId().generate(request));
    }

    @PostConstruct
    public void postConstruct() {
        ContextRegistry.getInstance().registerThreadLocalAccessor(new ObservationAwareSpanThreadLocalAccessor(tracer));
        ObservationThreadLocalAccessor.getInstance().setObservationRegistry(observationRegistry);
        Metrics.observationRegistry(observationRegistry);
    }

    @Bean
    @Profile("!local")
    public Sink logstashSink() {
        return new LogstashLogbackSink(new JsonHttpLogFormatter(), DEBUG);
    }

    @Bean
    @Profile("local")
    public Sink defaultLogbookSink() {
        return new DefaultSink(new DefaultHttpLogFormatter(), new DefaultHttpLogWriter());
    }

    @Bean
    public LogbookCreator.Builder logbook(CorrelationId correlationId, Sink sink) {
        return Logbook.builder()
                .sink(sink)
                .headerFilter(HeaderFilters.authorization())
                .correlationId(correlationId);
    }

    @Bean
    public NettyServerCustomizer logbookNettyServerCustomizer(
            ContextSnapshotFactory contextSnapshotFactory,
            LogbookCreator.Builder logbook
    ) {
        System.out.println("logbookNettyServerCustomizer Hit");
        return server -> server.doOnConnection(connection -> connection.addHandlerLast(
                new TracingChannelDuplexHandler(
                        new LogbookServerHandler(
                                logbook
                                        .requestFilter(HttpRequest::withoutBody)
                                        .responseFilter(HttpResponse::withoutBody)
                                        .build()
                        ),
                        contextSnapshotFactory
                )
        )).metrics(true, Function.identity());
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter datadogTraceIdRemapFilter(Tracer tracer) {
        System.out.println("datadogTraceIdRemapFilter Hit");
        return (exchange, chain) -> chain.filter(exchange)
                .contextWrite(ctx -> {
                    Optional.ofNullable(tracer.currentSpan())
                            .ifPresent(span -> MDC.put(DD_TRACE_NAME, span.context().traceId()));

                    return ctx;
                }).doFinally(_ -> MDC.remove(DD_TRACE_NAME));
    }

    @Bean
    @Order(2)
    public WebFilter appVersionRemapFilter(Tracer tracer) {
        System.out.println("appVersionRemapFilter Hit");
        return (exchange, chain) -> chain.filter(exchange)
                .contextWrite(ctx -> {
                    Optional.ofNullable(tracer.currentSpan())
                            .ifPresent(_ -> MDC.put(APP_VERSION,
                                    Optional.ofNullable(exchange.getRequest().getHeaders().get(APP_VERSION))
                                            .map(list -> list.get(0))
                                            .orElse(Strings.EMPTY)
                            ));

                    return ctx;
                }).doFinally(_ -> MDC.remove(APP_VERSION));
    }

    @Bean
    @Order(3)
    public WebFilter mdcUserRemapFilter(Tracer tracer) {
        System.out.println("mdcUserRemapFilter Hit");
        return (exchange, chain) -> chain.filter(exchange)
                .contextWrite(ctx -> {
                    Optional.ofNullable(tracer.currentSpan())
                            .ifPresent(_ -> MDC.put(MDC_USER_NAME, "This_is_user_id"));

                    return ctx;
                }).doFinally(_ -> MDC.remove(MDC_USER_NAME));
    }


    public static class TracingChannelDuplexHandler extends ChannelDuplexHandler {

        private final ChannelDuplexHandler delegate;
        private final ContextSnapshotFactory contextSnapshotFactory;

        public TracingChannelDuplexHandler(ChannelDuplexHandler delegate, ContextSnapshotFactory contextSnapshotFactory) {
            this.delegate = delegate;
            this.contextSnapshotFactory = contextSnapshotFactory;
        }

        @Override
        @SneakyThrows
        public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
            try (var _ = contextSnapshotFactory.setThreadLocalsFrom(ctx.channel())) {
                delegate.channelRead(ctx, msg);
            }catch (Exception e) {
                System.out.println("ERROR: " + e);
            }
        }

        @Override
        @SneakyThrows
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            try (var _ = contextSnapshotFactory.setThreadLocalsFrom(ctx.channel())) {
                delegate.write(ctx, msg, promise);
            }catch (Exception e) {
                System.out.println("ERROR: " + e);
            }
        }

        @Override
        public void flush(ChannelHandlerContext ctx) {
            try (var _ = contextSnapshotFactory.setThreadLocalsFrom(ctx.channel())) {
                ctx.flush();
            }catch (Exception e) {
                System.out.println("ERROR: " + e);
            }
        }
    }
}

