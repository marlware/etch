package com.etch.notificationservice.template;

import com.etch.events.NotificationChannel;
import com.etch.events.NotificationRequestedEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Renders the subject (email only) and body for a notification. Kept as
 * simple string templates rather than a templating engine (Thymeleaf,
 * FreeMarker, ...) since the message set is small and fixed; if it grows,
 * this is the seam where a real template engine would slot in without
 * touching the dispatch pipeline.
 */
@Component
public class NotificationMessageRenderer {

    public String renderSubject(NotificationChannel channel, NotificationRequestedEvent event) {
        return switch (channel) {
            case EMAIL -> "Your Etch order " + event.getOrderNumber() + " has been received";
            case SMS -> null;
        };
    }

    public String renderBody(NotificationChannel channel, NotificationRequestedEvent event) {
        String total = formatTotal(event.getOrderTotal());
        return switch (channel) {
            case EMAIL -> """
                    Hi,

                    Thanks for your order %s totaling $%s. We'll let you know once it ships.

                    -- Etch""".formatted(event.getOrderNumber(), total);
            case SMS -> "Etch: order %s ($%s) received. Thanks!".formatted(event.getOrderNumber(), total);
        };
    }

    private String formatTotal(BigDecimal total) {
        return total != null ? total.toPlainString() : "N/A";
    }
}
