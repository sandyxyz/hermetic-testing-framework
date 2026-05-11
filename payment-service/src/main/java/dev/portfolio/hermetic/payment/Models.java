package dev.portfolio.hermetic.payment;

import java.math.BigDecimal;

record Payment(
        String paymentId,
        long orderId,
        BigDecimal amount,
        PaymentStatus status
) {
}

record PaymentRequest(long orderId, BigDecimal amount) {
}

record GatewayResponse(boolean approved) {
}

enum PaymentStatus {
    SUCCESS,
    FAILED
}
