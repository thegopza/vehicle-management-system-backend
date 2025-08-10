package com.sam.vehicle_management_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sam.vehicle_management_system.payload.response.OTRequestDto;
import com.sam.vehicle_management_system.service.OTRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ot/requests")
@PreAuthorize("isAuthenticated()")
public class OTRequestController {

    @Autowired private OTRequestService otRequestService;
    @Autowired private ObjectMapper objectMapper;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createOtRequest(
            @RequestParam("data") String otRequestStr,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        try {
            com.sam.vehicle_management_system.payload.request.OTRequest requestPayload = objectMapper.readValue(otRequestStr, com.sam.vehicle_management_system.payload.request.OTRequest.class);
            otRequestService.createOtRequest(requestPayload, files);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating OT request: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<OTRequestDto> getOtRequestById(@PathVariable Long id) {
        OTRequestDto requestDto = otRequestService.getRequestById(id);
        return ResponseEntity.ok(requestDto);
    }

    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateOtRequest(
            @PathVariable Long id,
            @RequestParam("data") String otRequestStr,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        try {
            com.sam.vehicle_management_system.payload.request.OTRequest requestPayload =
                    objectMapper.readValue(otRequestStr, com.sam.vehicle_management_system.payload.request.OTRequest.class);
            OTRequestDto updatedRequest = otRequestService.updateOtRequest(id, requestPayload, files);
            return ResponseEntity.ok(updatedRequest);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating OT request: " + e.getMessage());
        }
    }
}