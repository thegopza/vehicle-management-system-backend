// path: src/main/java/com/sam/vehicle_management_system/repository/VehicleRepository.java
package com.sam.vehicle_management_system.repository;

import com.sam.vehicle_management_system.models.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    // JpaRepository มีเมธอดพื้นฐาน (findAll, findById, save, delete) ให้เราครบแล้ว
}