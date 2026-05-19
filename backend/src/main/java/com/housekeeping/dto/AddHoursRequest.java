package com.housekeeping.dto;

public class AddHoursRequest {
    private String orderId;
    private int additionalHours;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public int getAdditionalHours() { return additionalHours; }
    public void setAdditionalHours(int additionalHours) { this.additionalHours = additionalHours; }
}
