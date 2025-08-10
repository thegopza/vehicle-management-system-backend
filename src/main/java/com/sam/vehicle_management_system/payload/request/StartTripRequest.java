package com.sam.vehicle_management_system.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartTripRequest {
    private Long vehicleId;
    private Integer startMileage;
    private String destination;
    private String purpose;
}