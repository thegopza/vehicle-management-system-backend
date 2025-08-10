package com.sam.vehicle_management_system.controller;

import com.sam.vehicle_management_system.payload.response.MessageResponse;
import com.sam.vehicle_management_system.payload.response.VehicleAccidentsDto;
import com.sam.vehicle_management_system.service.AccidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/accidents")
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CAO')")
public class AccidentController {

    @Autowired
    private AccidentService accidentService;

    @GetMapping
    public ResponseEntity<List<VehicleAccidentsDto>> getAccidents() {
        return ResponseEntity.ok(accidentService.getGroupedAccidentReports());
    }

    @DeleteMapping("/vehicle/{vehicleId}")
    public ResponseEntity<?> clearAccidentsForVehicle(@PathVariable Long vehicleId) {
        accidentService.clearAccidentReportsForVehicle(vehicleId);
        return ResponseEntity.ok(new MessageResponse("Accident reports for vehicle cleared successfully."));
    }
}
