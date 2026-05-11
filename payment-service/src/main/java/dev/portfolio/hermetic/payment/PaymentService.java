package dev.portfolio.hermetic.payment;

import dev.portfolio.hermetic.payment.Models.GatewayResponse;
import dev.portfolio.hermetic.payment.Models.Payment;
import dev.portfolio.hermetic.payment.Models.PaymentRequest;
import dev.portfolio.hermetic.payment.Models.PaymentStatus;
import org.springframework.stereotype.Service;

@Service
class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayClient paymentGatewayClient;

    PaymentService(PaymentRepository paymentRepository, PaymentGatewayClient paymentGatewayClient) {
        this.paymentRepository = paymentRepository;
        this.paymentGatewayClient = paymentGatewayClient;
    }

    Payment process(PaymentRequest request) {
        GatewayResponse gatewayResponse = paymentGatewayClient.authorize(request.orderId(), request.amount());
        PaymentStatus status = gatewayResponse.approved() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        return paymentRepository.save(request.orderId(), request.amount(), status);
    }

    Payment get(String paymentId) {
        return paymentRepository.findById(paymentId);
    }
}
