package com.sam.vehicle_management_system.service;

import com.sam.vehicle_management_system.models.Trip;
import com.sam.vehicle_management_system.models.Vehicle;
import com.sam.vehicle_management_system.payload.request.UpdateTripHistoryRequest;
import com.sam.vehicle_management_system.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TripService {

    @Autowired
    private TripRepository tripRepository;

    public List<Trip> getAllTripHistoryWithLatestFlag() {
        List<Trip.TripStatus> statuses = List.of(
                Trip.TripStatus.COMPLETED,
                Trip.TripStatus.PENDING_KEY_RETURN
        );
        List<Trip> allHistory = tripRepository.findByUserIdAndStatusInOrderByStartTimeDesc(null, statuses);

        // --- ส่วนที่แก้ไข: ใช้ Logic ใหม่ที่แน่นอนกว่า ---
        // 1. จัดกลุ่ม Trip ทั้งหมดตาม ID ของรถ
        Map<Long, List<Trip>> tripsByVehicleId = allHistory.stream()
                .filter(trip -> trip.getVehicle() != null)
                .collect(Collectors.groupingBy(trip -> trip.getVehicle().getId()));

        // 2. หา Trip ที่ใหม่ที่สุดในแต่ละกลุ่ม แล้วกำหนด isLatest = true
        tripsByVehicleId.forEach((vehicleId, trips) -> {
            trips.stream()
                    .max(Comparator.comparing(Trip::getStartTime))
                    .ifPresent(latestTrip -> latestTrip.setLatest(true));
        });
        // ------------------------------------------------

        return allHistory;
    }

    @Transactional
    public void updateLatestTripDetails(Long tripId, UpdateTripHistoryRequest request) {
        Trip tripToUpdate = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found with id: " + tripId));

        Vehicle vehicle = tripToUpdate.getVehicle();

        // ตรวจสอบอีกครั้งให้แน่ใจว่าเป็น Trip ล่าสุดจริงๆ
        Optional<Trip> latestTripOpt = vehicle.getTrips().stream()
                .filter(t -> t.getStatus() == Trip.TripStatus.COMPLETED || t.getStatus() == Trip.TripStatus.PENDING_KEY_RETURN)
                .max(Comparator.comparing(Trip::getStartTime));

        if (latestTripOpt.isEmpty() || !latestTripOpt.get().getId().equals(tripId)) {
            throw new IllegalStateException("Cannot edit a trip that is not the latest for this vehicle.");
        }

        // อัปเดต Trip
        tripToUpdate.setEndMileage(request.getEndMileage());
        tripToUpdate.setFuelLevel(request.getFuelLevel());

        // อัปเดตข้อมูลล่าสุดของรถ
        vehicle.setLastMileage(request.getEndMileage());
        vehicle.setLastFuelLevel(request.getFuelLevel());
    }
}
