package com.example.orderprocessing.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        String productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
