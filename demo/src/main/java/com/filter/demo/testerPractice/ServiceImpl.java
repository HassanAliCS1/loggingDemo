package com.filter.demo.testerPractice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.stereotype.Service;

@Service
@Component
public class ServiceImpl implements ServiceA{

    ObjectMapper objectMapper = new ObjectMapper();

    Logger log = LoggerFactory.getLogger(ServiceImpl.class);
    String uri = "/v1/ayah/262/ar.alafasy";


    @Override
    public Mono<AyahRecord> retrieveAyah() {
    //TODO:OBJECTMAPPER for json
        ParameterizedTypeReference<AyahRecord<T>> type;

        WebClient ayahWebClient =
                WebClient.builder()
                        .baseUrl("http://api.alquran.cloud")
                        .build();

        return ayahWebClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(type).log()
                .doOnError(x -> log.error("Error" + x));
    }

}
