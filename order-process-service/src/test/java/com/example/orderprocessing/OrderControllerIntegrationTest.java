package com.example.orderprocessing;

import com.example.orderprocessing.entity.OrderEntity;
import com.example.orderprocessing.entity.OrderItemEntity;
import com.example.orderprocessing.enums.OrderStatus;
import com.example.orderprocessing.repository.OrderRepository;
import com.example.orderprocessing.scheduler.OrderStatusScheduler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = "spring.task.scheduling.enabled=false")
class OrderControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("order_processing_test")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusScheduler orderStatusScheduler;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    void shouldCreateAndRetrieveOrder() throws Exception {
        String requestBody = """
                {
                  "customerId": "CUST-1001",
                  "items": [
                    {
                      "productId": "P-101",
                      "productName": "Keyboard",
                      "quantity": 2,
                      "unitPrice": 45.50
                    }
                  ]
                }
                """;

        String createResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(91.00))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createdOrder = objectMapper.readTree(createResponse);
        long orderId = createdOrder.get("id").asLong();

        mockMvc.perform(get("/api/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value("CUST-1001"))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    @Test
    void shouldListOrdersByStatus() throws Exception {
        orderRepository.save(buildOrder("CUST-1001", OrderStatus.PENDING));
        orderRepository.save(buildOrder("CUST-1002", OrderStatus.PROCESSING));

        mockMvc.perform(get("/api/orders").param("status", "PROCESSING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("PROCESSING"));
    }

    @Test
    void shouldUpdateOrderStatus() throws Exception {
        OrderEntity savedOrder = orderRepository.save(buildOrder("CUST-1001", OrderStatus.PENDING));

        mockMvc.perform(patch("/api/orders/{orderId}/status", savedOrder.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "PROCESSING"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    void shouldCancelPendingOrder() throws Exception {
        OrderEntity savedOrder = orderRepository.save(buildOrder("CUST-1001", OrderStatus.PENDING));

        mockMvc.perform(post("/api/orders/{orderId}/cancel", savedOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldRejectCancellationForNonPendingOrder() throws Exception {
        OrderEntity savedOrder = orderRepository.save(buildOrder("CUST-1001", OrderStatus.SHIPPED));

        mockMvc.perform(post("/api/orders/{orderId}/cancel", savedOrder.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void schedulerShouldMovePendingOrdersToProcessing() {
        orderRepository.save(buildOrder("CUST-1001", OrderStatus.PENDING));
        orderRepository.save(buildOrder("CUST-1002", OrderStatus.SHIPPED));

        orderStatusScheduler.movePendingOrdersToProcessing();

        mockMvcStatusAssert();
    }

    private void mockMvcStatusAssert() {
        long pendingCount = orderRepository.findAllByStatusOrderByCreatedAtDesc(OrderStatus.PENDING).size();
        long processingCount = orderRepository.findAllByStatusOrderByCreatedAtDesc(OrderStatus.PROCESSING).size();

        org.junit.jupiter.api.Assertions.assertEquals(0, pendingCount);
        org.junit.jupiter.api.Assertions.assertEquals(1, processingCount);
    }

    private OrderEntity buildOrder(String customerId, OrderStatus status) {
        OrderEntity order = new OrderEntity();
        order.setCustomerId(customerId);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("25.00"));

        OrderItemEntity item = new OrderItemEntity();
        item.setProductId("P-100");
        item.setProductName("Demo Item");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("25.00"));
        item.setLineTotal(new BigDecimal("25.00"));
        order.addItem(item);
        return order;
    }
}
