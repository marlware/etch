package com.etch.orderservice.domain;

import com.etch.events.NotificationChannel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationChannelListConverterTest {

    private final NotificationChannelListConverter converter = new NotificationChannelListConverter();

    @Test
    void writesChannelsAsCommaSeparatedNames() {
        String column = converter.convertToDatabaseColumn(List.of(NotificationChannel.EMAIL, NotificationChannel.SMS));

        assertThat(column).isEqualTo("EMAIL,SMS");
    }

    @Test
    void writesNullAndEmptyListsAsEmptyString() {
        assertThat(converter.convertToDatabaseColumn(null)).isEmpty();
        assertThat(converter.convertToDatabaseColumn(List.of())).isEmpty();
    }

    @Test
    void readsCommaSeparatedNamesBackIntoChannels() {
        List<NotificationChannel> channels = converter.convertToEntityAttribute("SMS,EMAIL");

        assertThat(channels).containsExactly(NotificationChannel.SMS, NotificationChannel.EMAIL);
    }

    @Test
    void readsNullAndBlankColumnsAsEmptyList() {
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
        assertThat(converter.convertToEntityAttribute("  ")).isEmpty();
    }

    @Test
    void roundTripPreservesOrder() {
        List<NotificationChannel> original = List.of(NotificationChannel.SMS, NotificationChannel.EMAIL);

        assertThat(converter.convertToEntityAttribute(converter.convertToDatabaseColumn(original)))
                .isEqualTo(original);
    }

    @Test
    void rejectsUnknownChannelNames() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("EMAIL,CARRIER_PIGEON"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
