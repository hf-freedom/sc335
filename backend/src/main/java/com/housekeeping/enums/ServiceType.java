package com.housekeeping.enums;

import java.math.BigDecimal;

public enum ServiceType {
    CLEANING("日常保洁", new BigDecimal("50"), 2),
    DEEP_CLEANING("深度保洁", new BigDecimal("80"), 4),
    COOKING("做饭", new BigDecimal("60"), 2),
    NURSING("月嫂育儿", new BigDecimal("150"), 8),
    ELDER_CARE("老人陪护", new BigDecimal("70"), 4),
    REPAIR("家电维修", new BigDecimal("100"), 2);

    private final String description;
    private final BigDecimal pricePerHour;
    private final int minHours;

    ServiceType(String description, BigDecimal pricePerHour, int minHours) {
        this.description = description;
        this.pricePerHour = pricePerHour;
        this.minHours = minHours;
    }

    public String getDescription() { return description; }
    public BigDecimal getPricePerHour() { return pricePerHour; }
    public int getMinHours() { return minHours; }
}
