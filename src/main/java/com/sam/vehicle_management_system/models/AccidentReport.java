package com.sam.vehicle_management_system.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "accident_reports")
@Data
@NoArgsConstructor
public class AccidentReport {
    @Id
    private Long id;

    @OneToOne
    @JoinColumn(name = "trip_id")
    @MapsId
    @JsonBackReference("trip-accident")
    @ToString.Exclude // Prevent infinite loop
    @EqualsAndHashCode.Exclude // Prevent infinite loop
    private Trip trip;

    private LocalDateTime accidentTime;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "accident_photos", joinColumns = @JoinColumn(name = "accident_report_id"))
    @Column(name = "photo_url")
    private List<String> photoUrls;
}
