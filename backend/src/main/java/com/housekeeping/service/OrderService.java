package com.housekeeping.service;

import com.housekeeping.dto.*;
import com.housekeeping.entity.Aunt;
import com.housekeeping.entity.Order;
import com.housekeeping.entity.User;
import com.housekeeping.enums.OrderStatus;
import com.housekeeping.enums.ServiceType;
import com.housekeeping.storage.InMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private InMemoryStorage storage;

    public Order createOrder(CreateOrderRequest request) {
        User user = storage.getUser(request.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        ServiceType serviceType = request.getServiceType();
        if (request.getDurationHours() < serviceType.getMinHours()) {
            throw new RuntimeException("服务时长不能少于" + serviceType.getMinHours() + "小时");
        }

        Order order = new Order();
        order.setId(UUID.randomUUID().toString().replace("-", ""));
        order.setUserId(user.getId());
        order.setUserName(user.getName());
        order.setUserPhone(user.getPhone());
        order.setServiceType(serviceType);
        order.setAddress(request.getAddress() != null ? request.getAddress() : user.getAddress());
        order.setLatitude(request.getLatitude() != null ? request.getLatitude() : user.getLatitude());
        order.setLongitude(request.getLongitude() != null ? request.getLongitude() : user.getLongitude());
        order.setStartTime(request.getStartTime());
        order.setEndTime(request.getStartTime().plusHours(request.getDurationHours()));
        order.setDurationHours(request.getDurationHours());
        order.setTotalPrice(serviceType.getPricePerHour().multiply(BigDecimal.valueOf(request.getDurationHours())));
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        storage.saveOrder(order);
        return order;
    }

    public Order acceptOrder(AcceptOrderRequest request) {
        Order order = storage.getOrder(request.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("订单状态不允许接单");
        }

        Aunt aunt = storage.getAunt(request.getAuntId());
        if (aunt == null) {
            throw new RuntimeException("阿姨不存在");
        }

        validateAuntForOrder(aunt, order);

        order.setAuntId(aunt.getId());
        order.setAuntName(aunt.getName());
        order.setStatus(OrderStatus.ACCEPTED);
        order.setUpdatedAt(LocalDateTime.now());

        storage.saveOrder(order);
        return order;
    }

    private void validateAuntForOrder(Aunt aunt, Order order) {
        List<String> skillTags = aunt.getSkillTags();
        String requiredSkill = order.getServiceType().getDescription();
        if (skillTags == null || !skillTags.contains(requiredSkill)) {
            throw new RuntimeException("阿姨不具备该服务技能");
        }

        double distance = calculateDistance(
                aunt.getLatitude().doubleValue(), aunt.getLongitude().doubleValue(),
                order.getLatitude().doubleValue(), order.getLongitude().doubleValue()
        );
        if (distance > 20) {
            throw new RuntimeException("距离过远（超过20公里），无法接单");
        }

        int todayOrders = (int) storage.getAllOrders().stream()
                .filter(o -> aunt.getId().equals(o.getAuntId()))
                .filter(o -> o.getStatus() == OrderStatus.ACCEPTED || o.getStatus() == OrderStatus.IN_PROGRESS)
                .filter(o -> o.getStartTime().toLocalDate().equals(order.getStartTime().toLocalDate()))
                .count();
        if (todayOrders >= aunt.getMaxDailyOrders()) {
            throw new RuntimeException("今日接单已达上限");
        }

        boolean hasConflict = storage.getAllOrders().stream()
                .filter(o -> aunt.getId().equals(o.getAuntId()))
                .filter(o -> o.getStatus() == OrderStatus.ACCEPTED || o.getStatus() == OrderStatus.IN_PROGRESS)
                .anyMatch(o -> isTimeOverlap(o.getStartTime(), o.getEndTime(), order.getStartTime(), order.getEndTime()));
        if (hasConflict) {
            throw new RuntimeException("该时段已有订单，存在冲突");
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private boolean isTimeOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public Order cancelOrder(CancelOrderRequest request) {
        Order order = storage.getOrder(request.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("订单状态不允许取消");
        }

        long hoursToStart = Duration.between(LocalDateTime.now(), order.getStartTime()).toHours();
        BigDecimal penalty = BigDecimal.ZERO;

        if (order.getStatus() == OrderStatus.ACCEPTED || order.getStatus() == OrderStatus.IN_PROGRESS) {
            if (hoursToStart < 2) {
                penalty = order.getTotalPrice().multiply(new BigDecimal("0.5"));
            } else if (hoursToStart < 24) {
                penalty = order.getTotalPrice().multiply(new BigDecimal("0.3"));
            } else {
                penalty = order.getTotalPrice().multiply(new BigDecimal("0.1"));
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(request.getReason());
        order.setCancelPenalty(penalty);
        order.setUpdatedAt(LocalDateTime.now());

        storage.saveOrder(order);
        return order;
    }

    public Order rateOrder(RateOrderRequest request) {
        Order order = storage.getOrder(request.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("订单未完成，无法评价");
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("评分必须在1-5之间");
        }

        order.setRating(request.getRating());
        order.setComment(request.getComment());
        order.setUpdatedAt(LocalDateTime.now());

        if (order.getAuntId() != null) {
            Aunt aunt = storage.getAunt(order.getAuntId());
            if (aunt != null) {
                updateAuntRating(aunt, request.getRating());
                storage.saveAunt(aunt);
            }
        }

        storage.saveOrder(order);
        return order;
    }

    private void updateAuntRating(Aunt aunt, int newRating) {
        BigDecimal currentRating = aunt.getRating();
        int totalOrders = aunt.getTotalOrders() + 1;
        BigDecimal newAvgRating = currentRating.multiply(BigDecimal.valueOf(aunt.getTotalOrders()))
                .add(BigDecimal.valueOf(newRating))
                .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        aunt.setRating(newAvgRating);
        aunt.setTotalOrders(totalOrders);
        aunt.setUpdatedAt(LocalDateTime.now());
    }

    public Order addHours(AddHoursRequest request) {
        Order order = storage.getOrder(request.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.IN_PROGRESS && order.getStatus() != OrderStatus.ACCEPTED) {
            throw new RuntimeException("订单状态不允许加时");
        }
        if (request.getAdditionalHours() <= 0) {
            throw new RuntimeException("加时时长必须大于0");
        }

        LocalDateTime newEndTime = order.getEndTime().plusHours(request.getAdditionalHours());
        BigDecimal additionalPrice = order.getServiceType().getPricePerHour()
                .multiply(BigDecimal.valueOf(request.getAdditionalHours()));

        if (order.getAuntId() != null) {
            Aunt aunt = storage.getAunt(order.getAuntId());
            if (aunt != null) {
                boolean hasConflict = storage.getAllOrders().stream()
                        .filter(o -> aunt.getId().equals(o.getAuntId()))
                        .filter(o -> !o.getId().equals(order.getId()))
                        .filter(o -> o.getStatus() == OrderStatus.ACCEPTED || o.getStatus() == OrderStatus.IN_PROGRESS)
                        .anyMatch(o -> isTimeOverlap(order.getEndTime(), newEndTime, o.getStartTime(), o.getEndTime()));
                if (hasConflict) {
                    throw new RuntimeException("阿姨后续时段已有安排，无法加时");
                }
            }
        }

        order.setEndTime(newEndTime);
        order.setDurationHours(order.getDurationHours() + request.getAdditionalHours());
        order.setTotalPrice(order.getTotalPrice().add(additionalPrice));
        order.setUpdatedAt(LocalDateTime.now());

        storage.saveOrder(order);
        return order;
    }

    public Order startService(String orderId) {
        Order order = storage.getOrder(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.ACCEPTED) {
            throw new RuntimeException("订单状态不允许开始服务");
        }
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setUpdatedAt(LocalDateTime.now());
        storage.saveOrder(order);
        return order;
    }

    public Order completeService(String orderId) {
        Order order = storage.getOrder(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new RuntimeException("订单状态不允许完成服务");
        }
        order.setStatus(OrderStatus.COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());
        storage.saveOrder(order);
        return order;
    }

    public List<Order> getOrdersByUserId(String userId) {
        return storage.getAllOrders().stream()
                .filter(o -> userId.equals(o.getUserId()))
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public List<Order> getOrdersByAuntId(String auntId) {
        return storage.getAllOrders().stream()
                .filter(o -> auntId.equals(o.getAuntId()))
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public List<Order> getPendingOrders() {
        return storage.getAllOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .sorted((o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public List<Order> getPendingOrdersForAunt(String auntId) {
        Aunt aunt = storage.getAunt(auntId);
        if (aunt == null) {
            return Collections.emptyList();
        }

        return storage.getAllOrders().stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .filter(o -> {
                    List<String> skillTags = aunt.getSkillTags();
                    return skillTags != null && skillTags.contains(o.getServiceType().getDescription());
                })
                .filter(o -> {
                    double distance = calculateDistance(
                            aunt.getLatitude().doubleValue(), aunt.getLongitude().doubleValue(),
                            o.getLatitude().doubleValue(), o.getLongitude().doubleValue()
                    );
                    return distance <= 20;
                })
                .filter(o -> {
                    boolean hasConflict = storage.getAllOrders().stream()
                            .filter(existing -> auntId.equals(existing.getAuntId()))
                            .filter(existing -> existing.getStatus() == OrderStatus.ACCEPTED || existing.getStatus() == OrderStatus.IN_PROGRESS)
                            .anyMatch(existing -> isTimeOverlap(
                                    existing.getStartTime(), existing.getEndTime(),
                                    o.getStartTime(), o.getEndTime()
                            ));
                    return !hasConflict;
                })
                .sorted((o1, o2) -> {
                    if (o1.getTotalPrice().compareTo(o2.getTotalPrice()) != 0) {
                        return o2.getTotalPrice().compareTo(o1.getTotalPrice());
                    }
                    return o1.getCreatedAt().compareTo(o2.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    public List<Order> getAllOrders() {
        return storage.getAllOrders().stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public Order getOrderById(String id) {
        return storage.getOrder(id);
    }
}
