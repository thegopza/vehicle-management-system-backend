package com.sam.vehicle_management_system.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "ot_tasks")
@Data
@NoArgsConstructor
public class OTTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customRepairItem;

    @Column(columnDefinition = "TEXT")
    private String customFixDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ot_request_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude // Prevent infinite loop in toString()
    @EqualsAndHashCode.Exclude // Prevent infinite loop in equals() and hashCode()
    private OTRequest otRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkpoint_id")
    private Checkpoint checkpoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lane_id")
    private Lane lane;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;
}
