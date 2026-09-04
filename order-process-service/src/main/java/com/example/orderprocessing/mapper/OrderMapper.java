package com.example.orderprocessing.mapper;

import com.example.orderprocessing.dto.response.OrderItemResponse;
import com.example.orderprocessing.dto.response.OrderResponse;
import com.example.orderprocessing.entity.OrderEntity;
import com.example.orderprocessing.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(OrderEntity order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getVersion(),
                order.getItems().stream().map(this::toItemResponse).toList()
        );
    }

    public List<OrderResponse> toResponseList(List<OrderEntity> orders) {
        return orders.stream().map(this::toResponse).toList();
    }

    private OrderItemResponse toItemResponse(OrderItemEntity item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }
}
