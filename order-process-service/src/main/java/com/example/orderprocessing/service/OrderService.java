package com.example.orderprocessing.service;

import com.example.orderprocessing.dto.request.CreateOrderRequest;
import com.example.orderprocessing.dto.request.UpdateOrderStatusRequest;
import com.example.orderprocessing.dto.response.OrderResponse;
import com.example.orderprocessing.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(Long orderId);

    List<OrderResponse> listOrders(OrderStatus status);

    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);

    OrderResponse cancelOrder(Long orderId);

    int processPendingOrders();
}
