package com.filter.demo.controller;

import com.filter.demo.service.X1Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class Controller {

    Logger log = LoggerFactory.getLogger(Controller.class);


    private final X1Service svc;

    public Controller(X1Service svc) {
        this.svc = svc;
    }

    @GetMapping("/")
    public Mono<String> helloWorld(){
        log.info("Controller Hit");
        Mono<String> stringMono = svc.hitX1Api();
        return stringMono;
    }
}
