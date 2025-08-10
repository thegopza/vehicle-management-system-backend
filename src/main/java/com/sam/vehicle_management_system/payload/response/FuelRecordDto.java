package com.sam.vehicle_management_system.payload.response;

import com.sam.vehicle_management_system.models.EFuelStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for transferring FuelRecord data to the client.
 * This flattens the structure to include necessary details from related entities.
 */
@Data
@NoArgsConstructor
public class FuelRecordDto {

    private Long id;
    private LocalDateTime recordTimestamp;
    private Integer mileageAtRefuel;
    private BigDecimal amountPaid;
    private String receiptImageUrl;
    private EFuelStatus status;

    // Fields for clearing the bill
    private String project;
    private String serviceProvider;
    private BigDecimal amountWithdrawn;

    // Flattened data from related entities
    private String recordedByFirstName;
    private String recordedByLastName;
    private Long tripId;
    private String vehicleName;
    private String licensePlate;

}