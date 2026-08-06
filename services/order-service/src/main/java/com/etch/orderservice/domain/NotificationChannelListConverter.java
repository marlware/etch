package com.etch.orderservice.domain;

import com.etch.events.NotificationChannel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class NotificationChannelListConverter implements AttributeConverter<List<NotificationChannel>, String> {

    @Override
    public String convertToDatabaseColumn(List<NotificationChannel> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "";
        }
        return attribute.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    @Override
    public List<NotificationChannel> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }
        return Arrays.stream(dbData.split(","))
                .map(NotificationChannel::valueOf)
                .collect(Collectors.toList());
    }
}
