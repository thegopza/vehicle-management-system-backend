package com.sam.vehicle_management_system.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuelSummaryDto {
    private String filledBy;
    private LocalDate date;
    private BigDecimal amountPaid;
}
