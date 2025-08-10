package com.sam.vehicle_management_system.payload.response;

import lombok.Data;
import java.util.List;

@Data
public class VehicleAccidentsDto {
    private Long vehicleId;
    private String vehicleName;
    private String licensePlate;
    private List<AccidentReportDetailDto> accidentReports;
}
