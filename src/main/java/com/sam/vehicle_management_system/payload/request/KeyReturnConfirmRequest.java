package com.sam.vehicle_management_system.payload.request;

import lombok.Data;

@Data
public class KeyReturnConfirmRequest {
    private Integer endMileage;
    private String fuelLevel;
}