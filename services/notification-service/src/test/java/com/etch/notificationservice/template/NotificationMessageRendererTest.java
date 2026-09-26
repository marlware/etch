package com.etch.notificationservice.template;

import com.etch.events.NotificationChannel;
import com.etch.events.NotificationRequestedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationMessageRendererTest {

    private final NotificationMessageRenderer renderer = new NotificationMessageRenderer();

    private NotificationRequestedEvent event(BigDecimal total) {
        return new NotificationRequestedEvent("corr-1", 1L, 10L, "ORD-1001", total, NotificationChannel.EMAIL, "buyer@example.com");
    }

    @Test
    void emailSubjectMentionsTheOrderNumber() {
        String subject = renderer.renderSubject(NotificationChannel.EMAIL, event(new BigDecimal("49.99")));

        assertThat(subject).contains("ORD-1001");
    }

    @Test
    void smsHasNoSubject() {
        assertThat(renderer.renderSubject(NotificationChannel.SMS, event(new BigDecimal("49.99")))).isNull();
    }

    @Test
    void emailBodyIncludesOrderNumberAndTotal() {
        String body = renderer.renderBody(NotificationChannel.EMAIL, event(new BigDecimal("49.99")));

        assertThat(body).contains("ORD-1001").contains("$49.99");
    }

    @Test
    void smsBodyIsShortAndIncludesOrderNumberAndTotal() {
        String body = renderer.renderBody(NotificationChannel.SMS, event(new BigDecimal("49.99")));

        assertThat(body).isEqualTo("Etch: order ORD-1001 ($49.99) received. Thanks!");
    }

    @Test
    void totalIsRenderedWithoutScientificNotation() {
        String body = renderer.renderBody(NotificationChannel.SMS, event(new BigDecimal("1E+3")));

        assertThat(body).contains("$1000");
    }

    @Test
    void missingTotalFallsBackToPlaceholder() {
        String body = renderer.renderBody(NotificationChannel.SMS, event(null));

        assertThat(body).contains("$N/A");
    }
}
