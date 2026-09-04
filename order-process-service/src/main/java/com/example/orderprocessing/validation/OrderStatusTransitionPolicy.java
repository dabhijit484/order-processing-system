package com.example.orderprocessing.validation;

import com.example.orderprocessing.enums.OrderStatus;
import com.example.orderprocessing.exception.InvalidOrderStateException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class OrderStatusTransitionPolicy {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
            OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );

    public void assertTransitionAllowed(OrderStatus currentStatus, OrderStatus targetStatus) {
        if (currentStatus == targetStatus) {
            throw new InvalidOrderStateException("Order is already in status " + currentStatus);
        }

        Set<OrderStatus> allowedTargets = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowedTargets.contains(targetStatus)) {
            throw new InvalidOrderStateException(
                    "Invalid order status transition from %s to %s".formatted(currentStatus, targetStatus)
            );
        }
    }
}
