package com.etch.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void orderCreatedEventSurvivesAJsonRoundTrip() throws Exception {
        OrderCreatedEvent original = new OrderCreatedEvent(
                "corr-1", 42L, "ORD-1001", 7L, "buyer@example.com", "+15551234567",
                new BigDecimal("49.99"), List.of(NotificationChannel.EMAIL, NotificationChannel.SMS));

        OrderCreatedEvent copy = mapper.readValue(mapper.writeValueAsString(original), OrderCreatedEvent.class);

        assertThat(copy.getEventId()).isEqualTo(original.getEventId());
        assertThat(copy.getCorrelationId()).isEqualTo("corr-1");
        assertThat(copy.getOccurredAt()).isEqualTo(original.getOccurredAt());
        assertThat(copy.getOrderId()).isEqualTo(42L);
        assertThat(copy.getOrderNumber()).isEqualTo("ORD-1001");
        assertThat(copy.getTotal()).isEqualByComparingTo("49.99");
        assertThat(copy.getChannels()).containsExactly(NotificationChannel.EMAIL, NotificationChannel.SMS);
    }

    @Test
    void notificationRequestedEventSurvivesAJsonRoundTrip() throws Exception {
        NotificationRequestedEvent original = new NotificationRequestedEvent(
                "corr-2", 3L, 42L, "ORD-1001", new BigDecimal("10.00"), NotificationChannel.SMS, "+15551234567");

        NotificationRequestedEvent copy =
                mapper.readValue(mapper.writeValueAsString(original), NotificationRequestedEvent.class);

        assertThat(copy.getNotificationId()).isEqualTo(3L);
        assertThat(copy.getChannel()).isEqualTo(NotificationChannel.SMS);
        assertThat(copy.getRecipient()).isEqualTo("+15551234567");
        assertThat(copy.getOrderTotal()).isEqualByComparingTo("10.00");
    }

    @Test
    void everyEventGetsAUniqueIdAndTimestamp() {
        OrderCreatedEvent first = new OrderCreatedEvent();
        OrderCreatedEvent second = new OrderCreatedEvent();

        assertThat(first.getEventId()).isNotBlank().isNotEqualTo(second.getEventId());
        assertThat(first.getOccurredAt()).isNotNull();
    }
}
