package com.housekeeping.dto;

public class AcceptOrderRequest {
    private String orderId;
    private String auntId;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getAuntId() { return auntId; }
    public void setAuntId(String auntId) { this.auntId = auntId; }
}
