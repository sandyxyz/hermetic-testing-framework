package dev.portfolio.hermetic.payment;

import dev.portfolio.hermetic.payment.Models.Payment;
import dev.portfolio.hermetic.payment.Models.PaymentStatus;
import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class PaymentRepository {
    private final JdbcTemplate jdbcTemplate;

    PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Payment save(long orderId, BigDecimal amount, PaymentStatus status) {
        String paymentId = "pay-" + UUID.randomUUID();
        jdbcTemplate.update("""
                insert into payments (payment_id, order_id, amount, status)
                values (?, ?, ?, ?)
                """, paymentId, orderId, amount, status.name());
        return findById(paymentId);
    }

    Payment findById(String paymentId) {
        return jdbcTemplate.queryForObject("""
                select payment_id, order_id, amount, status
                from payments
                where payment_id = ?
                """, (rs, rowNum) -> new Payment(
                rs.getString("payment_id"),
                rs.getLong("order_id"),
                rs.getBigDecimal("amount"),
                PaymentStatus.valueOf(rs.getString("status"))), paymentId);
    }
}
