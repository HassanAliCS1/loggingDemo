package com.filter.demo.x1.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class X1ServiceImpl implements X1Service{

    private final WebClient X1_WebClientConfig;

    public X1ServiceImpl(WebClient x1WebClientConfig) {
        X1_WebClientConfig = x1WebClientConfig;
    }

    @Override
    public Mono<String> hitX1Api() {
        return X1_WebClientConfig.get()
                .uri("/")
                .retrieve().bodyToMono(String.class);
    }

}
