package com.sam.vehicle_management_system.controller;

import com.sam.vehicle_management_system.models.OTRequest;
import com.sam.vehicle_management_system.payload.request.RejectRequest;
import com.sam.vehicle_management_system.payload.response.OTRequestDto;
import com.sam.vehicle_management_system.service.OTApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ot")
public class OTApprovalController {

    @Autowired
    private OTApprovalService otApprovalService;

    @GetMapping("/approvals")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OTRequestDto>> getPendingRequests() {
        return ResponseEntity.ok(otApprovalService.getPendingRequests());
    }

    @PostMapping("/approvals/{id}/approve")
    @PreAuthorize("hasRole('MANAGER') or hasRole('CAO')")
    public ResponseEntity<OTRequestDto> approveRequest(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean overwrite) {
        return ResponseEntity.ok(otApprovalService.approveRequest(id, overwrite));
    }

    @PostMapping("/approvals/{id}/reject")
    @PreAuthorize("hasRole('MANAGER') or hasRole('CAO')")
    public ResponseEntity<OTRequestDto> rejectRequest(@PathVariable Long id, @RequestBody RejectRequest payload) {
        return ResponseEntity.ok(otApprovalService.rejectRequest(id, payload.getReason()));
    }

    @PostMapping("/approvals/{id}/review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OTRequestDto> reviewRequest(@PathVariable Long id) {
        return ResponseEntity.ok(otApprovalService.assistantReview(id));
    }

    @GetMapping("/history/my-requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OTRequestDto>> getMyOtHistory() {
        return ResponseEntity.ok(otApprovalService.getMyOtHistory());
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OTRequestDto>> getOtSummary(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(otApprovalService.getOtSummary(startDate, endDate));
    }
}