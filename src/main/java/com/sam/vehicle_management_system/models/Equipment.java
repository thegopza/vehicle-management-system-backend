package com.sam.vehicle_management_system.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ot_equipments")
@Data
@NoArgsConstructor
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // --- START: Workaround for outdated database schema ---
    // เพิ่มความสัมพันธ์นี้กลับเข้าไปชั่วคราว แต่กำหนดให้เป็นค่าว่างได้ (nullable = true)
    // เพื่อให้ Hibernate อัปเดต constraint ในฐานข้อมูล และแก้ปัญหา not-null violation
    // ความสัมพันธ์นี้จะไม่ได้ถูกใช้งานใน business logic ใหม่ของเรา
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lane_id", nullable = true)
    @JsonIgnore // ซ่อน field นี้จาก JSON response
    private Lane lane;
    // --- END: Workaround ---
}