package dev.portfolio.hermetic.order;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.portfolio.hermetic.order.Models.CreateOrderRequest;
import dev.portfolio.hermetic.order.Models.Order;
import dev.portfolio.hermetic.order.Models.OrderStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {
                "server.error.include-message=always",
                "server.error.include-stacktrace=always"
        })
class OrderServiceHermeticTest {
    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("orders")
            .withUsername("orders")
            .withPassword("orders");

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    static final WireMockServer paymentService = new WireMockServer(WireMockConfiguration.options().dynamicPort());

    static {
        paymentService.start();
        WireMock.configureFor("localhost", paymentService.port());
    }

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @AfterAll
    static void stopPaymentService() {
        paymentService.stop();
    }

    @DynamicPropertySource
    static void configureHermeticDependencies(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("payment-service.base-url", paymentService::baseUrl);
    }

    @Test
    void createsOrderWithMockedPaymentServiceAndIsolatedMySqlAndRedis() throws Exception {
        paymentService.stubFor(post("/payments")
                .withRequestBody(equalToJson("""
                        {
                          "orderId": 1,
                          "amount": 99.99
                        }
                        """))
                .willReturn(WireMock.okJson("""
                        {
                          "paymentId": "pay-123",
                          "status": "SUCCESS"
                        }
                        """)));

        ResponseEntity<Order> created = restTemplate.postForEntity(
                "/orders",
                new CreateOrderRequest("user-1", new BigDecimal("99.99")),
                Order.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().status()).isEqualTo(OrderStatus.PAYMENT_SUCCESS);
        assertThat(created.getBody().paymentId()).isEqualTo("pay-123");

        assertThat(created.getBody().id()).isPositive();

        ResponseEntity<String> fetched = restTemplate.getForEntity(
                "/orders/{orderId}",
                String.class,
                created.getBody().id());

        assertThat(fetched.getStatusCode())
                .withFailMessage("GET /orders/%s returned %s with body: %s",
                        created.getBody().id(),
                        fetched.getStatusCode(),
                        fetched.getBody())
                .isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readValue(fetched.getBody(), Order.class)).isEqualTo(created.getBody());
        verify(postRequestedFor(urlEqualTo("/payments")));
    }
}
