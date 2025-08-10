package com.sam.vehicle_management_system.payload.request;

import lombok.Data;

@Data
public class UpdateTripHistoryRequest {
    private Integer endMileage;
    private String fuelLevel;
}
