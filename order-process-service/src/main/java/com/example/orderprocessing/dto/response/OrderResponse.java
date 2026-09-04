package com.example.orderprocessing.dto.response;

import com.example.orderprocessing.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long version,
        List<OrderItemResponse> items
) {
}
