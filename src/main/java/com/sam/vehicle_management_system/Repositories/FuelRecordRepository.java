package com.sam.vehicle_management_system.repository;

import com.sam.vehicle_management_system.models.EFuelStatus;
import com.sam.vehicle_management_system.models.FuelRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FuelRecordRepository extends JpaRepository<FuelRecord, Long> {

    List<FuelRecord> findByStatus(EFuelStatus status);

    @Query("SELECT fr FROM FuelRecord fr JOIN FETCH fr.trip t JOIN FETCH t.vehicle WHERE fr.status = :status ORDER BY fr.recordTimestamp DESC")
    List<FuelRecord> findByStatusWithTripAndVehicle(@Param("status") EFuelStatus status);

    @Query("SELECT fr FROM FuelRecord fr JOIN FETCH fr.trip t JOIN FETCH t.vehicle WHERE fr.status = :status AND fr.recordedBy.id = :userId ORDER BY fr.recordTimestamp DESC")
    List<FuelRecord> findByStatusAndRecordedByIdWithDetails(@Param("status") EFuelStatus status, @Param("userId") Long userId);

    // --- เพิ่มเมธอดนี้เข้ามาใหม่ ---
    @Query("SELECT fr FROM FuelRecord fr " +
            "JOIN FETCH fr.recordedBy " +
            "JOIN FETCH fr.trip t " +
            "JOIN FETCH t.vehicle v " +
            "WHERE fr.recordTimestamp BETWEEN :startDate AND :endDate " +
            "AND (:vehicleId IS NULL OR v.id = :vehicleId) " +
            "ORDER BY fr.recordTimestamp DESC")
    List<FuelRecord> findAllWithDetailsByFilters(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("vehicleId") Long vehicleId
    );
    // ----------------------------
}
