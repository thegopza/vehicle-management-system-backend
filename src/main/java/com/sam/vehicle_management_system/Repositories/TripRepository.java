package com.sam.vehicle_management_system.repository;

import com.sam.vehicle_management_system.models.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByStatus(Trip.TripStatus status);

    Optional<Trip> findByUserIdAndStatus(Long userId, Trip.TripStatus status);

    @Query("SELECT t FROM Trip t WHERE (:userId IS NULL OR t.user.id = :userId) AND t.status IN :statuses ORDER BY t.startTime DESC")
    List<Trip> findByUserIdAndStatusInOrderByStartTimeDesc(@Param("userId") Long userId, @Param("statuses") List<Trip.TripStatus> statuses);

    // --- ส่วนที่แก้ไข ---
    // ลบ JOIN FETCH ar.reportedBy ที่ไม่จำเป็นและทำให้เกิด Error ออก
    @Query("SELECT DISTINCT t FROM Trip t " +
            "LEFT JOIN FETCH t.user " +
            "LEFT JOIN FETCH t.vehicle " +
            "LEFT JOIN FETCH t.fuelRecords fr " +
            "LEFT JOIN FETCH fr.recordedBy " +
            "LEFT JOIN FETCH t.accidentReport ar " +
            "WHERE t.status = com.sam.vehicle_management_system.models.Trip.TripStatus.COMPLETED " +
            "AND t.endTime BETWEEN :startDate AND :endDate")
    List<Trip> findAllCompletedBetweenDatesWithDetails(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    // --------------------
}
