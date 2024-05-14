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
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.LogbookCreator;
import org.zalando.logbook.netty.LogbookClientHandler;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@Configuration
public class X1_WebClientConfig {

    @Bean
    public WebClient arrayWebClient(
            WebClient.Builder builder,
            ContextSnapshotFactory contextSnapshotFactory,
            LogbookCreator.Builder logbookBuilder
    ) {
        Logbook logbook = logbookBuilder
                .headerFilter(_ -> HttpHeaders.empty())
                .bodyFilter((_, _) -> Strings.EMPTY)
                .build();

        HttpClient httpClient = HttpClient
                .newConnection()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .responseTimeout(Duration.ofSeconds(20))
                .doOnConnected(connection -> connection.addHandlerLast(
                        new TracingConfig.TracingChannelDuplexHandler(
                                new LogbookClientHandler(logbook),
                                contextSnapshotFactory
                        )
                ));

        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl("http://localhost:8081")
                .build();
    }
}
