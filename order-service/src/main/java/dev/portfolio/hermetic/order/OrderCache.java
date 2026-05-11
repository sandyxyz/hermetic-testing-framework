package dev.portfolio.hermetic.order;

import dev.portfolio.hermetic.order.Models.Order;
import dev.portfolio.hermetic.order.Models.OrderStatus;
import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
class OrderCache {
    private final StringRedisTemplate redisTemplate;

    OrderCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    void put(Order order) {
        String value = String.join("|",
                order.userId(),
                order.amount().toPlainString(),
                order.status().name(),
                order.paymentId() == null ? "" : order.paymentId());
        try {
            redisTemplate.opsForValue().set(key(order.id()), value);
        } catch (RuntimeException ignored) {
            // Redis is a cache only; MySQL remains the source of truth.
        }
    }

    Optional<Order> get(long orderId) {
        try {
            String value = redisTemplate.opsForValue().get(key(orderId));
            if (value == null) {
                return Optional.empty();
            }

            String[] parts = value.split("\\|", -1);
            if (parts.length != 4) {
                return Optional.empty();
            }

            return Optional.of(new Order(
                    orderId,
                    parts[0],
                    new BigDecimal(parts[1]),
                    OrderStatus.valueOf(parts[2]),
                    parts[3].isBlank() ? null : parts[3]));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    private String key(long orderId) {
        return "order:%d".formatted(orderId);
    }
}
