package com.filter.demo.testerPractice;

import reactor.core.publisher.Mono;

public interface ServiceA {

    Mono<AyahRecord> retrieveAyah();
}
