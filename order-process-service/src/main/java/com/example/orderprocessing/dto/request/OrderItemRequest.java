package com.example.orderprocessing.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderItemRequest(
        @NotBlank(message = "productId is required")
        String productId,
        @NotBlank(message = "productName is required")
        String productName,
        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be greater than zero")
        Integer quantity,
        @NotNull(message = "unitPrice is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "unitPrice must be non-negative")
        BigDecimal unitPrice
) {
}
