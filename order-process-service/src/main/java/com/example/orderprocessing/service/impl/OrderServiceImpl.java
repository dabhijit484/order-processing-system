package com.example.orderprocessing.service.impl;

import com.example.orderprocessing.dto.request.CreateOrderRequest;
import com.example.orderprocessing.dto.request.OrderItemRequest;
import com.example.orderprocessing.dto.request.UpdateOrderStatusRequest;
import com.example.orderprocessing.dto.response.OrderResponse;
import com.example.orderprocessing.entity.OrderEntity;
import com.example.orderprocessing.entity.OrderItemEntity;
import com.example.orderprocessing.enums.OrderStatus;
import com.example.orderprocessing.exception.OrderNotFoundException;
import com.example.orderprocessing.mapper.OrderMapper;
import com.example.orderprocessing.repository.OrderRepository;
import com.example.orderprocessing.service.OrderService;
import com.example.orderprocessing.validation.OrderStatusTransitionPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderStatusTransitionPolicy transitionPolicy;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderMapper orderMapper,
            OrderStatusTransitionPolicy transitionPolicy
    ) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.transitionPolicy = transitionPolicy;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        OrderEntity order = new OrderEntity();
        order.setCustomerId(request.customerId());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            OrderItemEntity item = new OrderItemEntity();
            item.setProductId(itemRequest.productId());
            item.setProductName(itemRequest.productName());
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(scaleCurrency(itemRequest.unitPrice()));

            BigDecimal lineTotal = scaleCurrency(itemRequest.unitPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.quantity())));
            item.setLineTotal(lineTotal);

            totalAmount = totalAmount.add(lineTotal);
            order.addItem(item);
        }

        order.setTotalAmount(scaleCurrency(totalAmount));
        OrderEntity savedOrder = orderRepository.saveAndFlush(order);
        log.info("Created order {} for customer {}", savedOrder.getId(), savedOrder.getCustomerId());
        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        return orderMapper.toResponse(findOrderById(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders(OrderStatus status) {
        List<OrderEntity> orders = status == null
                ? orderRepository.findAllByOrderByCreatedAtDesc()
                : orderRepository.findAllByStatusOrderByCreatedAtDesc(status);
        return orderMapper.toResponseList(orders);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        OrderEntity order = findOrderById(orderId);
        OrderStatus currentStatus = order.getStatus();
        transitionPolicy.assertTransitionAllowed(order.getStatus(), request.status());
        order.updateStatus(request.status());
        log.info("Updated order {} status from {} to {}", orderId, currentStatus, request.status());
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        OrderEntity order = findOrderById(orderId);
        transitionPolicy.assertTransitionAllowed(order.getStatus(), OrderStatus.CANCELLED);
        order.updateStatus(OrderStatus.CANCELLED);
        log.info("Cancelled order {}", orderId);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public int processPendingOrders() {
        List<OrderEntity> pendingOrders = orderRepository.findAllByStatusOrderByCreatedAtDesc(OrderStatus.PENDING);
        pendingOrders.forEach(order -> order.updateStatus(OrderStatus.PROCESSING));

        if (!pendingOrders.isEmpty()) {
            log.info("Moved {} pending orders to PROCESSING", pendingOrders.size());
        }

        return pendingOrders.size();
    }

    private OrderEntity findOrderById(Long orderId) {
        return orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private BigDecimal scaleCurrency(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
