package com.logging.x1.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    Logger log = LoggerFactory.getLogger(Controller.class);

    @GetMapping("/")
    public String helloWorld() {
        log.info("X1 Controller Hit");
        return "Hello from x1!";
    }
}