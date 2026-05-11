package dev.portfolio.hermetic.order;

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
        redisTemplate.opsForValue().set(key(order.id()), value);
    }

    Optional<Order> get(long orderId) {
        String value = redisTemplate.opsForValue().get(key(orderId));
        if (value == null) {
            return Optional.empty();
        }

        String[] parts = value.split("\\|", -1);
        return Optional.of(new Order(
                orderId,
                parts[0],
                new BigDecimal(parts[1]),
                OrderStatus.valueOf(parts[2]),
                parts[3].isBlank() ? null : parts[3]));
    }

    private String key(long orderId) {
        return "order:%d".formatted(orderId);
    }
}
