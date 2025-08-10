package com.sam.vehicle_management_system.controller;

import com.sam.vehicle_management_system.payload.response.MonthlyReportDto;
import com.sam.vehicle_management_system.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/monthly-summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAO')")
    public ResponseEntity<MonthlyReportDto> getMonthlySummary(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        MonthlyReportDto report = reportService.generateMonthlySummary(startDate, endDate);
        return ResponseEntity.ok(report);
    }
}
