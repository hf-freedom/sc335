package com.housekeeping.enums;

public enum OrderStatus {
    PENDING("待接单"),
    ACCEPTED("已接单"),
    IN_PROGRESS("服务中"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    EXPIRED("已过期");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}
