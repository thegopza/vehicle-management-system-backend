package com.sam.vehicle_management_system.payload.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class VehicleReportDto {
    private Long vehicleId;
    private String vehicleName;
    private String licensePlate;
    private int totalTrips;
    private int totalDistance;
    private BigDecimal totalFuelCost;
    private List<UserUsageDto> userUsage;
    private List<AccidentSummaryDto> accidentSummary;
    private List<FuelSummaryDto> fuelSummary;
}
