package com.sam.vehicle_management_system.payload.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class OverallFuelSummaryDto {
    private BigDecimal totalFuelCost;
    private Map<String, BigDecimal> fuelCostByVehicle;
}
