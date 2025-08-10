package com.sam.vehicle_management_system.controller;

import com.sam.vehicle_management_system.models.Lane;
import com.sam.vehicle_management_system.payload.request.*;
import com.sam.vehicle_management_system.payload.response.*;
import com.sam.vehicle_management_system.service.OTSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ot/settings")
public class OTSettingsController {

    @Autowired
    private OTSettingsService otSettingsService;

    // --- ส่วนการตั้งค่าส่วนตัว (สำหรับ Manager/CAO เท่านั้น) ---
    @PutMapping("/mode")
    @PreAuthorize("hasAnyRole('MANAGER', 'CAO')")
    public ResponseEntity<?> setOtMode(@RequestBody SetOtModeRequest request) {
        otSettingsService.setOtModeForCurrentUser(request.getMode());
        return ResponseEntity.ok(new MessageResponse("OT mode updated successfully!"));
    }

    @GetMapping("/assistants")
    @PreAuthorize("hasAnyRole('MANAGER', 'CAO')")
    public ResponseEntity<Set<UserDto>> getAssistants() {
        return ResponseEntity.ok(otSettingsService.getAssistants());
    }

    @PostMapping("/assistants")
    @PreAuthorize("hasAnyRole('MANAGER', 'CAO')")
    public ResponseEntity<?> addAssistant(@RequestBody AssistantRequest request) {
        otSettingsService.addAssistant(request.getAssistantId());
        return ResponseEntity.ok(new MessageResponse("Assistant added successfully!"));
    }

    @DeleteMapping("/assistants/{assistantId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CAO')")
    public ResponseEntity<?> removeAssistant(@PathVariable Long assistantId) {
        otSettingsService.removeAssistant(assistantId);
        return ResponseEntity.ok(new MessageResponse("Assistant removed successfully!"));
    }


    // --- ส่วนการจัดการด่านและเลน (สำหรับ Manager/CAO และ ผู้ช่วย) ---
    @PostMapping("/checkpoints")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CheckpointDto> createCheckpoint(@RequestBody CheckpointRequest request) {
        return ResponseEntity.ok(otSettingsService.createCheckpoint(request.getName()));
    }

    @GetMapping("/checkpoints")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CheckpointDto>> getMyCheckpoints() {
        return ResponseEntity.ok(otSettingsService.getCheckpointsForCurrentUser());
    }

    @DeleteMapping("/checkpoints/{checkpointId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteCheckpoint(@PathVariable Long checkpointId) {
        otSettingsService.deleteCheckpoint(checkpointId);
        return ResponseEntity.ok(new MessageResponse("Checkpoint deleted successfully!"));
    }

    @PostMapping("/checkpoints/{checkpointId}/lanes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LaneDto> createLane(@PathVariable Long checkpointId, @RequestBody LaneRequest request) {
        return ResponseEntity.ok(otSettingsService.createLane(checkpointId, request.getName()));
    }

    @DeleteMapping("/lanes/{laneId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteLane(@PathVariable Long laneId) {
        otSettingsService.deleteLane(laneId);
        return ResponseEntity.ok(new MessageResponse("Lane deleted successfully!"));
    }

    // --- ส่วนการจัดการอุปกรณ์ (Global Master List) ---
    @GetMapping("/equipments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquipmentDto>> getAllEquipments() {
        return ResponseEntity.ok(otSettingsService.getAllEquipments());
    }

    @PostMapping("/equipments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EquipmentDto> createEquipment(@RequestBody EquipmentRequest request) {
        return ResponseEntity.ok(otSettingsService.createEquipment(request.getName()));
    }

    @DeleteMapping("/equipments/{equipmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteEquipment(@PathVariable Long equipmentId) {
        otSettingsService.deleteEquipment(equipmentId);
        return ResponseEntity.ok(new MessageResponse("Equipment deleted successfully!"));
    }
}