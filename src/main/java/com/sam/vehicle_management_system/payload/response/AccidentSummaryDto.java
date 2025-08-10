package com.sam.vehicle_management_system.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccidentSummaryDto {
    private String reportedBy;
    private int count;
}
