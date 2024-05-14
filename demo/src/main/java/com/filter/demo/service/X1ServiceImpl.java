package com.filter.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
@Service
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
