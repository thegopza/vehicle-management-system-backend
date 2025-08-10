package com.sam.vehicle_management_system.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "trips")
@Data
@NoArgsConstructor
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer startMileage;
    private Integer endMileage;
    private String fuelLevel;
    private String destination;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @Enumerated(EnumType.STRING)
    private TripStatus status;

    @ManyToOne
    @JoinColumn(name = "returned_by_user_id")
    private User returnedBy;

    @OneToOne(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("trip-accident") // <-- เพิ่มชื่อเฉพาะ
    private AccidentReport accidentReport;

    // --- ส่วนที่เพิ่มเข้ามาใหม่ ---
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("trip-fuel") // <-- เพิ่มชื่อเฉพาะ
    private List<FuelRecord> fuelRecords;
    // ----------------------------

    @Transient
    private boolean isLatest = false;

    public enum TripStatus { IN_PROGRESS, PENDING_KEY_RETURN, COMPLETED, CANCELLED }
}
