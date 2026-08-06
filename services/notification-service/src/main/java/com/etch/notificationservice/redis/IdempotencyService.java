package com.etch.notificationservice.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Guards against double-processing the same Kafka message -- consumer
 * rebalances and at-least-once delivery mean a message can be redelivered
 * even after it was handled, so each handler claims a key before doing
 * any work and skips (rather than reprocesses) if the claim already
 * exists.
 */
@Service
public class IdempotencyService {

    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Attempts to atomically claim {@code key}. Returns {@code true} if
     * this call was the first to claim it (caller should proceed),
     * {@code false} if it was already claimed (caller should skip).
     */
    public boolean claim(String key) {
        return claim(key, DEFAULT_TTL);
    }

    public boolean claim(String key, Duration ttl) {
        Boolean firstClaim = redisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(firstClaim);
    }

    public void release(String key) {
        redisTemplate.delete(key);
    }
}
