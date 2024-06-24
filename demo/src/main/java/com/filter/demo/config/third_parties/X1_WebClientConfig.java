package com.filter.demo.config.third_parties;

import com.filter.demo.config.TracingConfig;
import com.filter.demo.x1.X1ResponseObjectMapper;
import io.micrometer.context.ContextSnapshotFactory;
import io.netty.channel.ChannelOption;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.zalando.logbook.*;
import org.zalando.logbook.netty.LogbookClientHandler;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.zalando.logbook.json.JsonPathBodyFilters.jsonPath;

@Configuration
public class X1_WebClientConfig {
    private static final int memorySize = 1048576;

    ExchangeStrategies strategies = ExchangeStrategies
            .builder()
            .codecs(cfg -> {
                cfg.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(new X1ResponseObjectMapper(), MediaType.APPLICATION_JSON));
                cfg.defaultCodecs().maxInMemorySize(memorySize);
            })
            .build();

    @Bean
    public Logbook logbookX1(CorrelationId correlationId, Sink sink) {
        return Logbook.builder()
                .sink(sink)
                .correlationId(correlationId)
                .headerFilter(_ -> HttpHeaders.empty())
                .build();
    }

    @Bean
    public WebClient X1WebClient(
            WebClient.Builder builder,
            ContextSnapshotFactory contextSnapshotFactory,
            Logbook logbookX1
    ) {
        HttpClient httpClient = HttpClient
                .newConnection()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .responseTimeout(Duration.ofSeconds(20))
                .doOnConnected(connection -> connection.addHandlerLast(
                        new TracingConfig.TracingChannelDuplexHandler(
                                new LogbookClientHandler(logbookX1),
                                contextSnapshotFactory
                        )
                ));

        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(org.springframework.http.HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .defaultHeader("response-content-type", "json")
                .exchangeStrategies(strategies)
                .baseUrl("http://localhost:8081")
                .build();
    }
}
