package org.team12.ticketing.concurrency.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisHealthIndicator implements HealthIndicator {

    private final StringRedisTemplate redisTemplate;

    public RedisHealthIndicator(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            return pong.equalsIgnoreCase("PONG")
                    ? Health.up().withDetail("redis", "UP").build()
                    : Health.down().withDetail("redis", "DOWN").build();
        } catch (Exception e) {
            return Health.down().withDetail("redis", "DOWN").withException(e).build();
        }
    }
}
