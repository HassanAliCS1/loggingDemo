package com.filter.demo.x1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static com.fasterxml.jackson.databind.DeserializationFeature.*;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

public class X1ResponseObjectMapper extends ObjectMapper {
    public X1ResponseObjectMapper() {
        registerModule(new JavaTimeModule());
        registerModule(new Jdk8Module());
        disable(FAIL_ON_UNKNOWN_PROPERTIES);
        disable(WRITE_DATES_AS_TIMESTAMPS);
        enable(ACCEPT_SINGLE_VALUE_AS_ARRAY);
        enable(READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        setObjectResponsePropertyNamingStrategy(SNAKE_CASE);
    }

    protected void setObjectResponsePropertyNamingStrategy(PropertyNamingStrategy propertyNamingStrategy) {
        super.setPropertyNamingStrategy(propertyNamingStrategy);
    }
}
