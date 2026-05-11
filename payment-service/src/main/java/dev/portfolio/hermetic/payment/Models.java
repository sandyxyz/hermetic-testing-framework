package dev.portfolio.hermetic.payment;

import java.math.BigDecimal;

public final class Models {
    private Models() {
    }

    public record Payment(
            String paymentId,
            long orderId,
            BigDecimal amount,
            PaymentStatus status
    ) {
    }

    public record PaymentRequest(long orderId, BigDecimal amount) {
    }

    public record GatewayResponse(boolean approved) {
    }

    public enum PaymentStatus {
        SUCCESS,
        FAILED
    }
}
