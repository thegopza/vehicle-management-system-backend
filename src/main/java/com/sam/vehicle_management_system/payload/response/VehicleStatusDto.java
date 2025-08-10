package com.sam.vehicle_management_system.payload.response;

import com.sam.vehicle_management_system.models.Vehicle;
import lombok.Data;

@Data
public class VehicleStatusDto {
    private Long id;
    private String name;
    private String licensePlate;
    private Boolean available;
    private Boolean active;
    private Integer lastMileage;
    private String lastFuelLevel;

    // Fields from Trip
    private String driverFirstName;
    private String driverLastName;
    private String destination;
    private String tripStatus;

    public static VehicleStatusDto fromVehicle(Vehicle vehicle) {
        VehicleStatusDto dto = new VehicleStatusDto();
        dto.setId(vehicle.getId());
        dto.setName(vehicle.getName());
        dto.setLicensePlate(vehicle.getLicensePlate());
        dto.setAvailable(vehicle.getAvailable());
        dto.setActive(vehicle.getActive());
        dto.setLastMileage(vehicle.getLastMileage());
        dto.setLastFuelLevel(vehicle.getLastFuelLevel());
        return dto;
    }
}
