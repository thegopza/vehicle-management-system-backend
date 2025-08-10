package com.sam.vehicle_management_system.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fuel_records")
@Data
@NoArgsConstructor
public class FuelRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    @JsonBackReference("trip-fuel")
    @ToString.Exclude // Prevent infinite loop
    @EqualsAndHashCode.Exclude // Prevent infinite loop
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "recorded_by_user_id", nullable = false)
    private User recordedBy;

    private LocalDateTime recordTimestamp;
    private Integer mileageAtRefuel;
    private BigDecimal amountPaid;
    private String receiptImageUrl;

    // Fields for clearing the bill
    private String project;
    private String serviceProvider;
    private BigDecimal amountWithdrawn;

    @Enumerated(EnumType.STRING)
    private EFuelStatus status;
}
