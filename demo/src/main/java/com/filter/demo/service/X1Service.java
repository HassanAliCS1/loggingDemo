package com.filter.demo.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

public interface X1Service {
    Mono<String> hitX1Api();
}
