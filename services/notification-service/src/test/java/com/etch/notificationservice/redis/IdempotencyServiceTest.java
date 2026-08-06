package com.etch.notificationservice.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private IdempotencyService idempotencyService;

    @BeforeEach
    void setUp() {
        // not every test exercises opsForValue() (release() doesn't), so this
        // stub is lenient rather than required on every run
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        idempotencyService = new IdempotencyService(redisTemplate);
    }

    @Test
    void claim_returnsTrueWhenKeyWasNotAlreadyPresent() {
        when(valueOperations.setIfAbsent(eq("k1"), eq("1"), any(Duration.class))).thenReturn(true);

        assertThat(idempotencyService.claim("k1")).isTrue();
    }

    @Test
    void claim_returnsFalseWhenKeyAlreadyClaimed() {
        when(valueOperations.setIfAbsent(eq("k1"), eq("1"), any(Duration.class))).thenReturn(false);

        assertThat(idempotencyService.claim("k1")).isFalse();
    }

    @Test
    void claim_usesTheSuppliedTtlWhenGiven() {
        Duration customTtl = Duration.ofSeconds(30);
        when(valueOperations.setIfAbsent("k2", "1", customTtl)).thenReturn(true);

        assertThat(idempotencyService.claim("k2", customTtl)).isTrue();
        verify(valueOperations).setIfAbsent("k2", "1", customTtl);
    }

    @Test
    void release_deletesTheKey() {
        idempotencyService.release("k1");

        verify(redisTemplate).delete("k1");
    }
}
