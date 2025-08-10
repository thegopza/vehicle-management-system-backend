package com.sam.vehicle_management_system.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "ot_dates")
@Data
@NoArgsConstructor
public class OTDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate workDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ot_request_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude // Prevent infinite loop in toString()
    @EqualsAndHashCode.Exclude // Prevent infinite loop in equals() and hashCode()
    private OTRequest otRequest;
}
