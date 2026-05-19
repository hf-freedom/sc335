package com.housekeeping.controller;

import com.housekeeping.common.Result;
import com.housekeeping.dto.*;
import com.housekeeping.entity.Order;
import com.housekeeping.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Result<Order> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.createOrder(request);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/accept")
    public Result<Order> acceptOrder(@RequestBody AcceptOrderRequest request) {
        try {
            Order order = orderService.acceptOrder(request);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/cancel")
    public Result<Order> cancelOrder(@RequestBody CancelOrderRequest request) {
        try {
            Order order = orderService.cancelOrder(request);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/rate")
    public Result<Order> rateOrder(@RequestBody RateOrderRequest request) {
        try {
            Order order = orderService.rateOrder(request);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/add-hours")
    public Result<Order> addHours(@RequestBody AddHoursRequest request) {
        try {
            Order order = orderService.addHours(request);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/start")
    public Result<Order> startService(@PathVariable String id) {
        try {
            Order order = orderService.startService(id);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/complete")
    public Result<Order> completeService(@PathVariable String id) {
        try {
            Order order = orderService.completeService(id);
            return Result.success(order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping
    public Result<List<Order>> getAllOrders() {
        return Result.success(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public Result<Order> getOrderById(@PathVariable String id) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @GetMapping("/user/{userId}")
    public Result<List<Order>> getOrdersByUserId(@PathVariable String userId) {
        return Result.success(orderService.getOrdersByUserId(userId));
    }

    @GetMapping("/aunt/{auntId}")
    public Result<List<Order>> getOrdersByAuntId(@PathVariable String auntId) {
        return Result.success(orderService.getOrdersByAuntId(auntId));
    }

    @GetMapping("/pending")
    public Result<List<Order>> getPendingOrders() {
        return Result.success(orderService.getPendingOrders());
    }

    @GetMapping("/pending/aunt/{auntId}")
    public Result<List<Order>> getPendingOrdersForAunt(@PathVariable String auntId) {
        return Result.success(orderService.getPendingOrdersForAunt(auntId));
    }
}
