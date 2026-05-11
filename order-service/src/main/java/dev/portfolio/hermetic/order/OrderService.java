package dev.portfolio.hermetic.order;

import dev.portfolio.hermetic.order.Models.CreateOrderRequest;
import dev.portfolio.hermetic.order.Models.Order;
import dev.portfolio.hermetic.order.Models.OrderStatus;
import dev.portfolio.hermetic.order.Models.PaymentResponse;
import org.springframework.stereotype.Service;

@Service
class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;
    private final OrderCache orderCache;

    OrderService(OrderRepository orderRepository, PaymentClient paymentClient, OrderCache orderCache) {
        this.orderRepository = orderRepository;
        this.paymentClient = paymentClient;
        this.orderCache = orderCache;
    }

    Order create(CreateOrderRequest request) {
        Order created = orderRepository.create(request.userId(), request.amount());
        PaymentResponse payment = paymentClient.process(created.id(), created.amount());
        OrderStatus status = "SUCCESS".equals(payment.status()) ? OrderStatus.PAYMENT_SUCCESS : OrderStatus.PAYMENT_FAILED;
        Order updated = orderRepository.updatePayment(created.id(), payment.paymentId(), status);
        orderCache.put(updated);
        return updated;
    }

    Order get(long orderId) {
        Order order = orderRepository.findById(orderId);
        orderCache.put(order);
        return order;
    }
}
