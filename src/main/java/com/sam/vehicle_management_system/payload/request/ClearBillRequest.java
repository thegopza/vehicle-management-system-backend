package com.sam.vehicle_management_system.payload.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ClearBillRequest {
    private String project;
    private String serviceProvider;
    private BigDecimal amountWithdrawn;
    private Integer mileageAtRefuel;
    private BigDecimal amountPaid;
}
