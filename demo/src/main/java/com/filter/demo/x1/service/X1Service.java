package com.filter.demo.x1.service;

import reactor.core.publisher.Mono;

public interface X1Service {
    Mono<String> hitX1Api();
}
