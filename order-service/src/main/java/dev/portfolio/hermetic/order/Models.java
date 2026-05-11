package dev.portfolio.hermetic.order;

import java.math.BigDecimal;

public final class Models {
    private Models() {
    }

    public record Order(
            long id,
            String userId,
            BigDecimal amount,
            OrderStatus status,
            String paymentId
    ) {
    }

    public record CreateOrderRequest(String userId, BigDecimal amount) {
    }

    public record PaymentResponse(String paymentId, String status) {
    }

    public enum OrderStatus {
        CREATED,
        PAYMENT_SUCCESS,
        PAYMENT_FAILED
    }
}
