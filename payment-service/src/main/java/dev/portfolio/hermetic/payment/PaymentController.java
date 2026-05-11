package dev.portfolio.hermetic.payment;

import dev.portfolio.hermetic.payment.Models.Payment;
import dev.portfolio.hermetic.payment.Models.PaymentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
class PaymentController {
    private final PaymentService paymentService;

    PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Payment create(@RequestBody PaymentRequest request) {
        return paymentService.process(request);
    }

    @GetMapping("/{paymentId}")
    Payment get(@PathVariable String paymentId) {
        return paymentService.get(paymentId);
    }
}
