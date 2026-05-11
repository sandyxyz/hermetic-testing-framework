package dev.portfolio.hermetic.order;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class PaymentClient {
    private final RestClient restClient;

    PaymentClient(@Value("${payment-service.base-url}") String baseUrl, RestClient.Builder builder) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    PaymentResponse process(long orderId, BigDecimal amount) {
        PaymentResponse response = restClient.post()
                .uri("/payments")
                .body(new PaymentRequest(orderId, amount))
                .retrieve()
                .body(PaymentResponse.class);

        if (response == null) {
            throw new IllegalStateException("Payment service returned no response");
        }
        return response;
    }

    private record PaymentRequest(long orderId, BigDecimal amount) {
    }
}
