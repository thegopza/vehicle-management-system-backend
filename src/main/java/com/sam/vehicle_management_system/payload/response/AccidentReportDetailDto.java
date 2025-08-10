package com.sam.vehicle_management_system.payload.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AccidentReportDetailDto {
    private Long id;
    private Long tripId;
    private LocalDateTime accidentTime;
    private String description;
    private String location;
    private List<String> photoUrls;
    private String reporterFirstName;
    private String reporterLastName;
}
