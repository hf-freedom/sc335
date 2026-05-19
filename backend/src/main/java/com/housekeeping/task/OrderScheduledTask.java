package com.housekeeping.task;

import com.housekeeping.entity.Order;
import com.housekeeping.enums.OrderStatus;
import com.housekeeping.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderScheduledTask.class);

    @Autowired
    private OrderService orderService;

    @Scheduled(fixedRate = 60000)
    public void scanUpcomingOrders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime upcoming = now.plusHours(1);

        List<Order> upcomingOrders = orderService.getAllOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.ACCEPTED)
                .filter(o -> o.getStartTime().isAfter(now) && o.getStartTime().isBefore(upcoming))
                .toList();

        if (!upcomingOrders.isEmpty()) {
            logger.info("检测到{}个即将开始的订单", upcomingOrders.size());
            for (Order order : upcomingOrders) {
                logger.info("订单{}将在{}开始，请提醒阿姨{}",
                        order.getId(),
                        order.getStartTime(),
                        order.getAuntName());
            }
        }
    }

    @Scheduled(fixedRate = 300000)
    public void scanExpiredPendingOrders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.minusMinutes(30);

        List<Order> expiredOrders = orderService.getAllOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .filter(o -> o.getCreatedAt().isBefore(expireTime))
                .toList();

        if (!expiredOrders.isEmpty()) {
            logger.info("检测到{}个超时未接单的订单，将自动标记为过期", expiredOrders.size());
            for (Order order : expiredOrders) {
                order.setStatus(OrderStatus.EXPIRED);
                order.setUpdatedAt(now);
                logger.info("订单{}已超时，自动标记为过期", order.getId());
            }
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void scanBadRatedOrders() {
        List<Order> badRatedOrders = orderService.getAllOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .filter(o -> o.getRating() != null && o.getRating() <= 2)
                .toList();

        if (!badRatedOrders.isEmpty()) {
            logger.warn("检测到{}个差评订单，需要重点关注", badRatedOrders.size());
            for (Order order : badRatedOrders) {
                logger.warn("订单{} - 阿姨: {}, 评分: {}, 评价: {}",
                        order.getId(),
                        order.getAuntName(),
                        order.getRating(),
                        order.getComment());
            }
        }
    }

    @Scheduled(fixedRate = 120000)
    public void autoStartInProgressOrders() {
        LocalDateTime now = LocalDateTime.now();

        List<Order> shouldStartOrders = orderService.getAllOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.ACCEPTED)
                .filter(o -> o.getStartTime().isBefore(now) || o.getStartTime().isEqual(now))
                .toList();

        for (Order order : shouldStartOrders) {
            try {
                orderService.startService(order.getId());
                logger.info("订单{}已自动开始服务", order.getId());
            } catch (Exception e) {
                logger.error("自动开始订单{}失败: {}", order.getId(), e.getMessage());
            }
        }
    }
}
