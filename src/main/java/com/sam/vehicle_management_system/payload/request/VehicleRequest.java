// path: src/main/java/com/sam/vehicle_management_system/payload/request/VehicleRequest.java
package com.sam.vehicle_management_system.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleRequest {
    private String name;
    private String licensePlate;
    private Integer lastMileage;
    private String lastFuelLevel;
}