package com.sam.vehicle_management_system.payload.response;

import com.sam.vehicle_management_system.models.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSimpleDto {
    private Long id;
    private String licensePlate;

    public static VehicleSimpleDto fromEntity(Vehicle vehicle) {
        return new VehicleSimpleDto(vehicle.getId(), vehicle.getLicensePlate());
    }
}
