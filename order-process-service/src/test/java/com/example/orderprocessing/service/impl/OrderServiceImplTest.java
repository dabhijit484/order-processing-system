package com.example.orderprocessing.service.impl;

import com.example.orderprocessing.dto.request.CreateOrderRequest;
import com.example.orderprocessing.dto.request.OrderItemRequest;
import com.example.orderprocessing.dto.request.UpdateOrderStatusRequest;
import com.example.orderprocessing.dto.response.OrderResponse;
import com.example.orderprocessing.entity.OrderEntity;
import com.example.orderprocessing.entity.OrderItemEntity;
import com.example.orderprocessing.enums.OrderStatus;
import com.example.orderprocessing.exception.InvalidOrderStateException;
import com.example.orderprocessing.mapper.OrderMapper;
import com.example.orderprocessing.repository.OrderRepository;
import com.example.orderprocessing.validation.OrderStatusTransitionPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(
                orderRepository,
                new OrderMapper(),
                new OrderStatusTransitionPolicy()
        );
    }

    @Test
    void shouldCreateOrderAndCalculateTotals() {
        CreateOrderRequest request = new CreateOrderRequest(
                "CUST-1001",
                List.of(
                        new OrderItemRequest("P-101", "Keyboard", 2, new BigDecimal("45.50")),
                        new OrderItemRequest("P-202", "Mouse", 1, new BigDecimal("15.00"))
                )
        );

        when(orderRepository.saveAndFlush(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 1L);
            ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now());
            ReflectionTestUtils.setField(order, "updatedAt", LocalDateTime.now());
            ReflectionTestUtils.setField(order, "version", 0L);

            long itemId = 1L;
            for (OrderItemEntity item : order.getItems()) {
                ReflectionTestUtils.setField(item, "id", itemId++);
            }
            return order;
        });

        OrderResponse response = orderService.createOrder(request);

        assertEquals(OrderStatus.PENDING, response.status());
        assertEquals(new BigDecimal("106.00"), response.totalAmount());
        assertEquals(2, response.items().size());
        assertEquals(new BigDecimal("91.00"), response.items().get(0).lineTotal());
        assertEquals(new BigDecimal("15.00"), response.items().get(1).lineTotal());
    }

    @Test
    void shouldCancelPendingOrder() {
        OrderEntity order = buildOrderEntity(OrderStatus.PENDING);
        when(orderRepository.findWithItemsById(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, response.status());
    }

    @Test
    void shouldRejectInvalidStatusTransition() {
        OrderEntity order = buildOrderEntity(OrderStatus.SHIPPED);
        when(orderRepository.findWithItemsById(1L)).thenReturn(Optional.of(order));

        assertThrows(
                InvalidOrderStateException.class,
                () -> orderService.updateOrderStatus(1L, new UpdateOrderStatusRequest(OrderStatus.PROCESSING))
        );
    }

    private OrderEntity buildOrderEntity(OrderStatus status) {
        OrderEntity order = new OrderEntity();
        ReflectionTestUtils.setField(order, "id", 1L);
        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(order, "updatedAt", LocalDateTime.now());
        ReflectionTestUtils.setField(order, "version", 0L);
        order.setCustomerId("CUST-1001");
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("20.00"));

        OrderItemEntity item = new OrderItemEntity();
        ReflectionTestUtils.setField(item, "id", 1L);
        item.setProductId("P-101");
        item.setProductName("Keyboard");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("20.00"));
        item.setLineTotal(new BigDecimal("20.00"));
        order.addItem(item);
        return order;
    }
}
