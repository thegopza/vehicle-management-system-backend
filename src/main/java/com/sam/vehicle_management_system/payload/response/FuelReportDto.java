package com.sam.vehicle_management_system.payload.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class FuelReportDto {
    private List<FuelRecordDto> records;
    private int totalCount;
    private BigDecimal totalAmount;

    public FuelReportDto(List<FuelRecordDto> records) {
        this.records = records;
        this.totalCount = records.size();
        this.totalAmount = records.stream()
                .map(FuelRecordDto::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
