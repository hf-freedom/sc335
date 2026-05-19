package com.housekeeping.dto;

import com.housekeeping.enums.ServiceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateOrderRequest {
    private String userId;
    private ServiceType serviceType;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime startTime;
    private int durationHours;
    private String remark;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
