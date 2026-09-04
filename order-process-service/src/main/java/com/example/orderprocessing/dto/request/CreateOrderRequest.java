package com.example.orderprocessing.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank(message = "customerId is required")
        String customerId,
        @NotEmpty(message = "items must not be empty")
        List<@Valid OrderItemRequest> items
) {
}
