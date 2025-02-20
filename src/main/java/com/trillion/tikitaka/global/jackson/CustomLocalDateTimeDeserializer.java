package com.trillion.tikitaka.global.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER_WITH_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");
    private static final DateTimeFormatter FORMATTER_DATE_ONLY = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText().trim();
        if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return LocalDate.parse(value, FORMATTER_DATE_ONLY).atStartOfDay();
        } else {
            return LocalDateTime.parse(value, FORMATTER_WITH_TIME);
        }
    }
}
