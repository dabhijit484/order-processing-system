package com.example.orderprocessing.dto.request;

import com.example.orderprocessing.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "status is required")
        OrderStatus status
) {
}
