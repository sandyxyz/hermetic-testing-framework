package dev.portfolio.hermetic.payment;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class PaymentGatewayClient {
    private final RestClient restClient;

    PaymentGatewayClient(@Value("${payment-gateway.base-url}") String baseUrl, RestClient.Builder builder) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    GatewayResponse authorize(long orderId, BigDecimal amount) {
        GatewayResponse response = restClient.post()
                .uri("/gateway/authorize")
                .body(new GatewayRequest(orderId, amount))
                .retrieve()
                .body(GatewayResponse.class);

        if (response == null) {
            throw new IllegalStateException("Payment gateway returned no response");
        }
        return response;
    }

    private record GatewayRequest(long orderId, BigDecimal amount) {
    }
}
