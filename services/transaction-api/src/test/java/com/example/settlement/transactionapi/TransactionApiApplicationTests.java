package com.example.settlement.transactionapi;

import com.example.settlement.transactionapi.dto.CreateTransactionRequest;
import com.example.settlement.transactionapi.dto.TransactionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionApiApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createsAndFetchesTransaction() {
        CreateTransactionRequest request = new CreateTransactionRequest("txn-001", new BigDecimal("125.50"), "THB");

        ResponseEntity<TransactionResponse> createResponse =
                restTemplate.postForEntity(url("/api/transactions"), request, TransactionResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().status()).isEqualTo("PENDING");

        Long id = createResponse.getBody().id();
        ResponseEntity<TransactionResponse> getResponse =
                restTemplate.getForEntity(url("/api/transactions/" + id), TransactionResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().reference()).isEqualTo("txn-001");
    }

    @Test
    void rejectsDuplicateReference() {
        CreateTransactionRequest request = new CreateTransactionRequest("txn-dup", new BigDecimal("10.00"), "USD");
        restTemplate.postForEntity(url("/api/transactions"), request, TransactionResponse.class);

        ResponseEntity<TransactionResponse> secondResponse =
                restTemplate.postForEntity(url("/api/transactions"), request, TransactionResponse.class);

        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
