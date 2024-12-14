package com.cdc.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public enum ObjectMapperUtil {
    INSTANCE;

    private final ObjectMapper objectMapper;

    ObjectMapperUtil() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}