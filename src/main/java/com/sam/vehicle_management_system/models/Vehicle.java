package com.sam.vehicle_management_system.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String licensePlate;
    private Boolean available = true;

    @Column(name = "is_active")
    private Boolean active = true;

    private Integer lastMileage;
    private String lastFuelLevel;

    // --- ส่วนที่เพิ่มเข้ามาใหม่ ---
    @OneToMany(mappedBy = "vehicle", fetch = FetchType.LAZY)
    @JsonIgnore // ป้องกัน Loop อ้างอิงไม่รู้จบ
    private List<Trip> trips;
    // ----------------------------
}
