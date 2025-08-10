package com.sam.vehicle_management_system.controller;

import com.sam.vehicle_management_system.models.Vehicle;
import com.sam.vehicle_management_system.payload.request.VehicleRequest;
import com.sam.vehicle_management_system.payload.response.MessageResponse;
import com.sam.vehicle_management_system.payload.response.VehicleStatusDto;
import com.sam.vehicle_management_system.repository.VehicleRepository;
import com.sam.vehicle_management_system.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.sam.vehicle_management_system.payload.response.VehicleSimpleDto;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleService vehicleService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        return ResponseEntity.ok(vehicles);
    }

    // --- *** START: ส่วนที่แก้ไข *** ---
    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()") // เปลี่ยนเป็น isAuthenticated() เพื่อให้ทุก Role เข้าถึงได้
    public ResponseEntity<List<VehicleStatusDto>> getVehicleStatuses() {
        return ResponseEntity.ok(vehicleService.getAllVehicleStatuses());
    }
    // --- *** END: ส่วนที่แก้ไข *** ---

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAO')")
    public ResponseEntity<?> addVehicle(@RequestBody VehicleRequest vehicleRequest) {
        Vehicle vehicle = new Vehicle();
        vehicle.setName(vehicleRequest.getName());
        vehicle.setLicensePlate(vehicleRequest.getLicensePlate());
        vehicle.setLastMileage(vehicleRequest.getLastMileage());
        vehicle.setLastFuelLevel(vehicleRequest.getLastFuelLevel());
        vehicle.setAvailable(true);
        vehicle.setActive(true);
        vehicleRepository.save(vehicle);
        return ResponseEntity.status(201).body(new MessageResponse("Vehicle added successfully!"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CAO')")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        return vehicleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all-simple")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<VehicleSimpleDto>> getAllSimpleVehicles() {
        List<VehicleSimpleDto> vehicles = vehicleService.getAllSimpleVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAO')")
    public ResponseEntity<?> updateVehicle(@PathVariable Long id, @RequestBody VehicleRequest vehicleRequest) {
        return vehicleRepository.findById(id).map(vehicle -> {
            vehicle.setName(vehicleRequest.getName());
            vehicle.setLicensePlate(vehicleRequest.getLicensePlate());
            vehicle.setLastMileage(vehicleRequest.getLastMileage());
            vehicle.setLastFuelLevel(vehicleRequest.getLastFuelLevel());
            vehicleRepository.save(vehicle);
            return ResponseEntity.ok(new MessageResponse("Vehicle updated successfully!"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAO')")
    public ResponseEntity<?> deactivateVehicle(@PathVariable Long id) {
        return vehicleRepository.findById(id).map(vehicle -> {
            vehicle.setActive(false);
            vehicleRepository.save(vehicle);
            return ResponseEntity.ok(new MessageResponse("Vehicle deactivated successfully!"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAO')")
    public ResponseEntity<?> reactivateVehicle(@PathVariable Long id) {
        return vehicleRepository.findById(id).map(vehicle -> {
            vehicle.setActive(true);
            vehicleRepository.save(vehicle);
            return ResponseEntity.ok(new MessageResponse("Vehicle reactivated successfully!"));
        }).orElse(ResponseEntity.notFound().build());
    }
}