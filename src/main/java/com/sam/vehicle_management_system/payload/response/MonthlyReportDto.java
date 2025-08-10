package com.sam.vehicle_management_system.payload.response;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class MonthlyReportDto {
    private OverallFuelSummaryDto overallFuelSummary = new OverallFuelSummaryDto();
    private List<VehicleReportDto> vehicleReports = new ArrayList<>();
}
