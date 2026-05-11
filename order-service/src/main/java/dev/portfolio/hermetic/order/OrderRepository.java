package dev.portfolio.hermetic.order;

import dev.portfolio.hermetic.order.Models.Order;
import dev.portfolio.hermetic.order.Models.OrderStatus;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
class OrderRepository {
    private final JdbcTemplate jdbcTemplate;

    OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Order create(String userId, BigDecimal amount) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into orders (user_id, amount, status)
                    values (?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, userId);
            statement.setBigDecimal(2, amount);
            statement.setString(3, OrderStatus.CREATED.name());
            return statement;
        }, keyHolder);
        return findById(keyHolder.getKey().longValue());
    }

    Order updatePayment(long orderId, String paymentId, OrderStatus status) {
        jdbcTemplate.update("""
                update orders
                set payment_id = ?, status = ?
                where id = ?
                """, paymentId, status.name(), orderId);
        return findById(orderId);
    }

    Order findById(long orderId) {
        return jdbcTemplate.queryForObject("""
                select id, user_id, amount, status, payment_id
                from orders
                where id = ?
                """, (rs, rowNum) -> new Order(
                rs.getLong("id"),
                rs.getString("user_id"),
                rs.getBigDecimal("amount"),
                OrderStatus.valueOf(rs.getString("status")),
                rs.getString("payment_id")), orderId);
    }
}
