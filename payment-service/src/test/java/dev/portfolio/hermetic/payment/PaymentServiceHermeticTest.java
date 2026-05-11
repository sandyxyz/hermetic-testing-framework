package dev.portfolio.hermetic.payment;

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
import dev.portfolio.hermetic.payment.Models.Payment;
import dev.portfolio.hermetic.payment.Models.PaymentRequest;
import dev.portfolio.hermetic.payment.Models.PaymentStatus;
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
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
class PaymentServiceHermeticTest {
    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("payments")
            .withUsername("payments")
            .withPassword("payments");

    static final WireMockServer paymentGateway = new WireMockServer(WireMockConfiguration.options().dynamicPort());

    static {
        paymentGateway.start();
        WireMock.configureFor("localhost", paymentGateway.port());
    }

    @Autowired
    TestRestTemplate restTemplate;

    @AfterAll
    static void stopPaymentGateway() {
        paymentGateway.stop();
    }

    @DynamicPropertySource
    static void configureHermeticDependencies(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("payment-gateway.base-url", paymentGateway::baseUrl);
    }

    @Test
    void processesPaymentWithMockedGatewayAndIsolatedMySql() {
        paymentGateway.stubFor(post("/gateway/authorize")
                .withRequestBody(equalToJson("""
                        {
                          "orderId": 42,
                          "amount": 50.00
                        }
                        """))
                .willReturn(WireMock.okJson("""
                        {
                          "approved": true
                        }
                        """)));

        ResponseEntity<Payment> created = restTemplate.postForEntity(
                "/payments",
                new PaymentRequest(42, new BigDecimal("50.00")),
                Payment.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().status()).isEqualTo(PaymentStatus.SUCCESS);

        ResponseEntity<Payment> fetched = restTemplate.getForEntity(
                "/payments/{paymentId}",
                Payment.class,
                created.getBody().paymentId());

        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody()).isEqualTo(created.getBody());
        verify(postRequestedFor(urlEqualTo("/gateway/authorize")));
    }
}
