package com.sam.vehicle_management_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sam.vehicle_management_system.models.*;
import com.sam.vehicle_management_system.payload.request.EndTripRequest;
import com.sam.vehicle_management_system.payload.request.StartTripRequest;
import com.sam.vehicle_management_system.payload.response.MessageResponse;
import com.sam.vehicle_management_system.repository.AccidentReportRepository;
import com.sam.vehicle_management_system.repository.TripRepository;
import com.sam.vehicle_management_system.repository.UserRepository;
import com.sam.vehicle_management_system.repository.VehicleRepository;
import com.sam.vehicle_management_system.service.FileStorageService;
import com.sam.vehicle_management_system.payload.request.UpdateTripHistoryRequest;
import com.sam.vehicle_management_system.service.TripService;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/trips")
public class TripController {

    @Autowired private TripRepository tripRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AccidentReportRepository accidentReportRepository;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TripService tripService;

    @PostMapping("/start")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('CAO')")
    @Transactional
    public ResponseEntity<?> startTrip(@RequestBody StartTripRequest startTripRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = ((UserDetails) authentication.getPrincipal()).getUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Error: Current user not found."));

        Vehicle vehicle = vehicleRepository.findById(startTripRequest.getVehicleId())
                .orElse(null);

        if (vehicle == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Error: Vehicle not found!"));
        }
        if (vehicle.getAvailable() == null || !vehicle.getAvailable()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse("Error: Vehicle is not available!"));
        }

        vehicle.setAvailable(false);

        Trip newTrip = new Trip();
        newTrip.setUser(currentUser);
        newTrip.setVehicle(vehicle);
        newTrip.setStartTime(LocalDateTime.now());
        newTrip.setStartMileage(startTripRequest.getStartMileage());
        newTrip.setDestination(startTripRequest.getDestination());
        newTrip.setStatus(Trip.TripStatus.IN_PROGRESS);

        tripRepository.save(newTrip);

        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Trip started successfully!"));
    }

    @PostMapping(value = "/end/{tripId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('CAO')")
    @Transactional
    public ResponseEntity<?> endTrip(@PathVariable Long tripId,
                                     @RequestParam("data") String endTripRequestStr,
                                     @RequestParam(value = "files", required = false) MultipartFile[] files) {

        try {
            EndTripRequest endTripRequest = objectMapper.readValue(endTripRequestStr, EndTripRequest.class);

            Trip trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new RuntimeException("Trip not found with id: " + tripId));

            if (trip.getStatus() != Trip.TripStatus.IN_PROGRESS && trip.getStatus() != Trip.TripStatus.PENDING_KEY_RETURN) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Error: This trip cannot be modified."));
            }

            String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
            User returningUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Error: Current user not found."));

            trip.setReturnedBy(returningUser);
            trip.setEndTime(LocalDateTime.now());
            trip.setEndMileage(endTripRequest.getEndMileage());
            trip.setFuelLevel(endTripRequest.getFuelLevel());
            trip.setNotes(endTripRequest.getNotes());
            trip.setStatus(Trip.TripStatus.PENDING_KEY_RETURN);

            if (endTripRequest.isHasAccident()) {
                AccidentReport accidentReport = trip.getAccidentReport();
                if (accidentReport == null) {
                    accidentReport = new AccidentReport();
                    accidentReport.setTrip(trip);
                    trip.setAccidentReport(accidentReport);
                }

                accidentReport.setAccidentTime(LocalDateTime.now());
                accidentReport.setDescription(endTripRequest.getAccidentDescription());
                accidentReport.setLocation(endTripRequest.getAccidentLocation());

                List<String> photoUrls = new ArrayList<>();
                if (files != null && files.length > 0) {
                    if (accidentReport.getPhotoUrls() != null) {
                        accidentReport.getPhotoUrls().forEach(fileStorageService::deleteFile);
                    }
                    Arrays.stream(files).forEach(file -> {
                        String fileName = fileStorageService.storeFile(file);
                        photoUrls.add(fileName);
                    });
                } else if (accidentReport.getPhotoUrls() != null) {
                    photoUrls.addAll(accidentReport.getPhotoUrls());
                }

                accidentReport.setPhotoUrls(photoUrls);
            } else {
                if (trip.getAccidentReport() != null) {
                    if(trip.getAccidentReport().getPhotoUrls() != null){
                        trip.getAccidentReport().getPhotoUrls().forEach(fileStorageService::deleteFile);
                    }
                    trip.setAccidentReport(null);
                }
            }

            tripRepository.save(trip);

            return ResponseEntity.ok(new MessageResponse("Trip information submitted successfully."));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error processing request: " + e.getMessage()));
        }
    }

    // --- *** START: ส่วนที่แก้ไข *** ---
    @GetMapping("/my/active-trips")
    @PreAuthorize("isAuthenticated()") // เปลี่ยนเป็น isAuthenticated()
    public ResponseEntity<List<Trip>> getMyActiveTrips() {
        // --- *** END: ส่วนที่แก้ไข *** ---
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: Current user not found."));

        List<Trip.TripStatus> activeStatuses = List.of(
                Trip.TripStatus.IN_PROGRESS,
                Trip.TripStatus.PENDING_KEY_RETURN
        );

        List<Trip> activeTrips = tripRepository.findByUserIdAndStatusInOrderByStartTimeDesc(currentUser.getId(), activeStatuses);

        return ResponseEntity.ok(activeTrips);
    }

    // --- *** START: ส่วนที่แก้ไข *** ---
    @GetMapping("/my/history")
    @PreAuthorize("isAuthenticated()") // เปลี่ยนเป็น isAuthenticated()
    public ResponseEntity<List<Trip>> getMyTripHistory() {
        // --- *** END: ส่วนที่แก้ไข *** ---
        String username = ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Error: Current user not found."));

        List<Trip.TripStatus> completedStatuses = List.of(
                Trip.TripStatus.COMPLETED
        );

        List<Trip> history = tripRepository.findByUserIdAndStatusInOrderByStartTimeDesc(currentUser.getId(), completedStatuses);

        return ResponseEntity.ok(history);
    }

    @GetMapping("/pending-process")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Trip>> getAllPendingProcessTrips() {
        List<Trip.TripStatus> statuses = List.of(
                Trip.TripStatus.IN_PROGRESS,
                Trip.TripStatus.PENDING_KEY_RETURN
        );
        List<Trip> pendingTrips = tripRepository.findByUserIdAndStatusInOrderByStartTimeDesc(null, statuses);
        return ResponseEntity.ok(pendingTrips);
    }

    @GetMapping("/history/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('CAO')")
    public ResponseEntity<List<Trip>> getAllTripHistory() {
        List<Trip.TripStatus> statuses = List.of(
                Trip.TripStatus.COMPLETED,
                Trip.TripStatus.PENDING_KEY_RETURN
        );
        List<Trip> allHistory = tripRepository.findByUserIdAndStatusInOrderByStartTimeDesc(null, statuses);
        return ResponseEntity.ok(allHistory);
    }

    @PutMapping("/history/{tripId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAO')")
    public ResponseEntity<?> updateTripHistory(@PathVariable Long tripId, @RequestBody UpdateTripHistoryRequest request) {
        try {
            tripService.updateLatestTripDetails(tripId, request);
            return ResponseEntity.ok(new MessageResponse("Trip history updated successfully!"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage()));
        }
    }
}