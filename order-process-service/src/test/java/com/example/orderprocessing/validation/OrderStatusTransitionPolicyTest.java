package com.example.orderprocessing.validation;

import com.example.orderprocessing.enums.OrderStatus;
import com.example.orderprocessing.exception.InvalidOrderStateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderStatusTransitionPolicyTest {

    private final OrderStatusTransitionPolicy policy = new OrderStatusTransitionPolicy();

    @Test
    void shouldAllowPendingToProcessingTransition() {
        assertDoesNotThrow(() -> policy.assertTransitionAllowed(OrderStatus.PENDING, OrderStatus.PROCESSING));
    }

    @Test
    void shouldRejectTransitionFromProcessingToCancelled() {
        assertThrows(
                InvalidOrderStateException.class,
                () -> policy.assertTransitionAllowed(OrderStatus.PROCESSING, OrderStatus.CANCELLED)
        );
    }

    @Test
    void shouldRejectTransitionToSameState() {
        assertThrows(
                InvalidOrderStateException.class,
                () -> policy.assertTransitionAllowed(OrderStatus.SHIPPED, OrderStatus.SHIPPED)
        );
    }
}
