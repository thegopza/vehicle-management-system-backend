package com.sam.vehicle_management_system.payload.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FuelRecordRequest {
    private Integer mileageAtRefuel;
    private BigDecimal amountPaid;
}
