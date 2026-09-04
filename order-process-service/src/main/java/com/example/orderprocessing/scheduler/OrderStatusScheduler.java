package com.example.orderprocessing.scheduler;

import com.example.orderprocessing.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusScheduler.class);

    private final OrderService orderService;

    public OrderStatusScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedRateString = "${app.scheduling.pending-to-processing-rate-ms:300000}")
    public void movePendingOrdersToProcessing() {
        int updatedCount = orderService.processPendingOrders();
        log.info("Pending order scheduler completed, updatedCount={}", updatedCount);
    }
}
