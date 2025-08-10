package com.sam.vehicle_management_system.service;

import com.sam.vehicle_management_system.models.Trip;
import com.sam.vehicle_management_system.models.Vehicle;
import com.sam.vehicle_management_system.payload.response.VehicleStatusDto;
import com.sam.vehicle_management_system.repository.TripRepository;
import com.sam.vehicle_management_system.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sam.vehicle_management_system.payload.response.VehicleSimpleDto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private TripRepository tripRepository;

    @Transactional(readOnly = true)
    public List<VehicleStatusDto> getAllVehicleStatuses() {
        // 1. ดึงรถยนต์ทั้งหมดที่ active อยู่
        List<Vehicle> vehicles = vehicleRepository.findAll().stream()
                .filter(v -> v.getActive() != null && v.getActive())
                .collect(Collectors.toList());

        // 2. ดึง Trip ทั้งหมดที่ยังไม่เสร็จสิ้นสมบูรณ์ (กำลังใช้งาน หรือ รอคืนกุญแจ)
        List<Trip.TripStatus> activeStatuses = Arrays.asList(
                Trip.TripStatus.IN_PROGRESS,
                Trip.TripStatus.PENDING_KEY_RETURN
        );
        List<Trip> activeTrips = tripRepository.findByUserIdAndStatusInOrderByStartTimeDesc(null, activeStatuses);

        // 3. แปลง Vehicle เป็น DTO และเติมข้อมูลจาก Trip ถ้ามี
        return vehicles.stream().map(vehicle -> {
            VehicleStatusDto dto = VehicleStatusDto.fromVehicle(vehicle);

            // ถ้ารถไม่ว่าง ให้หา Trip ที่เกี่ยวข้อง
            if (vehicle.getAvailable() != null && !vehicle.getAvailable()) {
                activeTrips.stream()
                        .filter(trip -> trip.getVehicle().getId().equals(vehicle.getId()))
                        .findFirst() // หา Trip ล่าสุดของรถคันนี้ที่ยังไม่เสร็จ
                        .ifPresent(trip -> {
                            // เติมข้อมูลจาก Trip ลงใน DTO
                            if (trip.getUser() != null) {
                                dto.setDriverFirstName(trip.getUser().getFirstName());
                                dto.setDriverLastName(trip.getUser().getLastName());
                            }
                            dto.setDestination(trip.getDestination());
                            dto.setTripStatus(trip.getStatus().name()); // เพิ่มสถานะของ Trip
                        });
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehicleSimpleDto> getAllSimpleVehicles() {
        return vehicleRepository.findAll().stream()
                .filter(v -> v.getActive() != null && v.getActive())
                .map(VehicleSimpleDto::fromEntity)
                .collect(Collectors.toList());
    }
}
