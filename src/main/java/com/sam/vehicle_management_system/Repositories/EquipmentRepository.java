package com.sam.vehicle_management_system.repository;

import com.sam.vehicle_management_system.models.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    // ไม่ต้องมีเมธอดเพิ่มเติมในนี้
}