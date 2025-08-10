package com.sam.vehicle_management_system.repository;

import com.sam.vehicle_management_system.models.AccidentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccidentReportRepository extends JpaRepository<AccidentReport, Long> {

    Optional<AccidentReport> findByTripId(Long tripId);

    @Query("SELECT ar FROM AccidentReport ar LEFT JOIN FETCH ar.photoUrls ORDER BY ar.accidentTime DESC")
    List<AccidentReport> findAllWithPhotosOrderByAccidentTimeDesc();

    // --- ส่วนที่เพิ่มเข้ามาใหม่: ค้นหารายงานทั้งหมดจาก ID ของรถ ---
    List<AccidentReport> findAllByTripVehicleId(Long vehicleId);
    // ----------------------------------------------------
}
