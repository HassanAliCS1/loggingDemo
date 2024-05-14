package com.filter.demo.config.third_parties;

import com.filter.demo.config.TracingConfig;
import io.micrometer.context.ContextSnapshotFactory;
import io.netty.channel.ChannelOption;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
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

    @Bean
    public Logbook logbookX1(CorrelationId correlationId, Sink sink) {
        return Logbook.builder()
                .sink(sink)
                .correlationId(correlationId)
                .headerFilter(_ -> HttpHeaders.empty())
                .bodyFilter(jsonPath("$.appKey").replace("***"))
                .bodyFilter(jsonPath("$.userToken").replace("***"))
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
                .baseUrl("http://localhost:8081")
                .build();
    }
}
