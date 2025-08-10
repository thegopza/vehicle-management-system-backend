package com.sam.vehicle_management_system.payload.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class OTDateDto {
    private Long id;
    private LocalDate workDate;
}