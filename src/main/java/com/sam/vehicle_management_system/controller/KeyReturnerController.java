package com.sam.vehicle_management_system.controller;

import com.sam.vehicle_management_system.models.EFuelStatus; // <-- Import ใหม่
import com.sam.vehicle_management_system.models.FuelRecord; // <-- Import ใหม่
import com.sam.vehicle_management_system.models.Trip;
import com.sam.vehicle_management_system.models.Vehicle;
import com.sam.vehicle_management_system.payload.request.KeyReturnConfirmRequest;
import com.sam.vehicle_management_system.payload.response.MessageResponse;
import com.sam.vehicle_management_system.repository.FuelRecordRepository; // <-- Import ใหม่
import com.sam.vehicle_management_system.repository.TripRepository;
import com.sam.vehicle_management_system.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/key-return")
public class KeyReturnerController {

    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private VehicleRepository vehicleRepository;

    // --- ส่วนที่เพิ่มเข้ามา ---
    @Autowired
    private FuelRecordRepository fuelRecordRepository;
    // -------------------------


    @GetMapping("/pending")
    @PreAuthorize("hasRole('KEY_RETURNER') or hasRole('ADMIN')")
    public ResponseEntity<List<Trip>> getPendingKeyReturnTrips() {
        List<Trip> pendingTrips = tripRepository.findByStatus(Trip.TripStatus.PENDING_KEY_RETURN);
        return ResponseEntity.ok(pendingTrips);
    }

    @PostMapping("/confirm/{tripId}")
    @PreAuthorize("hasRole('KEY_RETURNER') or hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> confirmKeyReturn(@PathVariable Long tripId, @RequestBody KeyReturnConfirmRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found with id: " + tripId));

        if (trip.getStatus() != Trip.TripStatus.PENDING_KEY_RETURN) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Error: This trip is not pending key return."));
        }

        // 1. อัปเดตข้อมูล Trip ด้วยข้อมูลล่าสุดจาก Key Returner
        trip.setEndMileage(request.getEndMileage());
        trip.setFuelLevel(request.getFuelLevel());
        trip.setStatus(Trip.TripStatus.COMPLETED);

        // 2. อัปเดตสถานะรถ
        Vehicle vehicle = trip.getVehicle();
        vehicle.setAvailable(true);
        vehicle.setLastMileage(trip.getEndMileage());
        vehicle.setLastFuelLevel(trip.getFuelLevel());

        // --- 3. LOGIC ใหม่ที่เพิ่มเข้ามา: อัปเดตสถานะบิลน้ำมัน ---
        List<FuelRecord> relatedFuelRecords = trip.getFuelRecords();
        if (relatedFuelRecords != null && !relatedFuelRecords.isEmpty()) {
            for (FuelRecord record : relatedFuelRecords) {
                if (record.getStatus() == EFuelStatus.RECORDED) {
                    record.setStatus(EFuelStatus.PENDING_CLEARANCE);
                }
            }
            fuelRecordRepository.saveAll(relatedFuelRecords);
        }
        // --------------------------------------------------------

        return ResponseEntity.ok(new MessageResponse("Key return confirmed. Trip is now completed and fuel bills are pending clearance."));
    }
}