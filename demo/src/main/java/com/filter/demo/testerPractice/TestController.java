package com.filter.demo.testerPractice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController{

    Logger log = LoggerFactory.getLogger(TestController.class);
    private final ServiceA svc;

    public TestController(ServiceA svc) {
        this.svc = svc;
    }

    @GetMapping("/test")
    public Mono<AyahRecord> helloWorld(){
        log.info("Controller Hit");
        return svc.retrieveAyah();
    }
}
