package com.sam.vehicle_management_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sam.vehicle_management_system.models.*;
import com.sam.vehicle_management_system.payload.request.ClearBillRequest;
import com.sam.vehicle_management_system.payload.request.FuelRecordRequest;
import com.sam.vehicle_management_system.payload.response.FuelRecordDto;
import com.sam.vehicle_management_system.payload.response.FuelReportDto;
import com.sam.vehicle_management_system.payload.response.MessageResponse;
import com.sam.vehicle_management_system.repository.FuelRecordRepository;
import com.sam.vehicle_management_system.repository.TripRepository;
import com.sam.vehicle_management_system.repository.UserRepository;
import com.sam.vehicle_management_system.security.services.UserDetailsImpl;
import com.sam.vehicle_management_system.service.FileStorageService;
import com.sam.vehicle_management_system.service.FuelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/fuel")
public class FuelController {

    @Autowired private FuelRecordRepository fuelRecordRepository;
    @Autowired private TripRepository tripRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private FuelService fuelService;

    @PostMapping(value = "/record/{tripId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> recordFuel(
            @PathVariable Long tripId,
            @RequestParam("data") String fuelRecordRequestStr,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            FuelRecordRequest request = objectMapper.readValue(fuelRecordRequestStr, FuelRecordRequest.class);

            Trip trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new RuntimeException("Trip not found with id: " + tripId));

            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User currentUser = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            FuelRecord fuelRecord = new FuelRecord();
            fuelRecord.setTrip(trip);
            fuelRecord.setRecordedBy(currentUser);
            fuelRecord.setRecordTimestamp(LocalDateTime.now());
            fuelRecord.setMileageAtRefuel(request.getMileageAtRefuel());
            fuelRecord.setAmountPaid(request.getAmountPaid());
            fuelRecord.setStatus(EFuelStatus.RECORDED);

            if (file != null && !file.isEmpty()) {
                String fileName = fileStorageService.storeFile(file);
                fuelRecord.setReceiptImageUrl(fileName);
            }

            fuelRecordRepository.save(fuelRecord);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Fuel record created successfully!"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error creating fuel record: " + e.getMessage()));
        }
    }

    // API 2: ดึงรายการบิลที่รอเคลียร์ (เฉพาะของตัวเอง)
    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FuelRecordDto>> getPendingFuelRecords() {
        // ดึง ID ของผู้ใช้ที่ Login อยู่
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = userDetails.getId();

        // เรียก Service โดยส่ง ID ของผู้ใช้ไปด้วย
        List<FuelRecordDto> pendingRecordsDto = fuelService.getPendingClearanceRecordsForUser(currentUserId);

        return ResponseEntity.ok(pendingRecordsDto);
    }

    @GetMapping("/report")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAO')")
    public ResponseEntity<FuelReportDto> getFuelReport(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "vehicleId", required = false) Long vehicleId) {

        FuelReportDto report = fuelService.getFuelReport(startDate, endDate, vehicleId);
        return ResponseEntity.ok(report);
    }

    // API 3: เคลียร์บิล
    @PutMapping(value = "/clear/{recordId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> clearBill(
            @PathVariable Long recordId,
            @RequestParam("data") String clearBillRequestStr,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            ClearBillRequest request = objectMapper.readValue(clearBillRequestStr, ClearBillRequest.class);

            FuelRecord fuelRecord = fuelRecordRepository.findById(recordId)
                    .orElseThrow(() -> new RuntimeException("Fuel record not found with id: " + recordId));

            if (fuelRecord.getStatus() != EFuelStatus.PENDING_CLEARANCE) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Error: This bill is not pending clearance."));
            }

            fuelRecord.setProject(request.getProject());
            fuelRecord.setServiceProvider(request.getServiceProvider());
            fuelRecord.setAmountWithdrawn(request.getAmountWithdrawn());
            fuelRecord.setMileageAtRefuel(request.getMileageAtRefuel());
            fuelRecord.setAmountPaid(request.getAmountPaid());
            fuelRecord.setStatus(EFuelStatus.CLEARED);

            if (file != null && !file.isEmpty()) {
                if (fuelRecord.getReceiptImageUrl() != null) {
                    fileStorageService.deleteFile(fuelRecord.getReceiptImageUrl());
                }
                String fileName = fileStorageService.storeFile(file);
                fuelRecord.setReceiptImageUrl(fileName);
            }

            fuelRecordRepository.save(fuelRecord);
            return ResponseEntity.ok(new MessageResponse("Fuel bill cleared successfully!"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error clearing fuel bill: " + e.getMessage()));
        }
    }
}
