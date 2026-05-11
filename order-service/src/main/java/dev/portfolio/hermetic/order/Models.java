package dev.portfolio.hermetic.order;

import java.math.BigDecimal;

record Order(
        long id,
        String userId,
        BigDecimal amount,
        OrderStatus status,
        String paymentId
) {
}

record CreateOrderRequest(String userId, BigDecimal amount) {
}

record PaymentResponse(String paymentId, String status) {
}

enum OrderStatus {
    CREATED,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED
}
