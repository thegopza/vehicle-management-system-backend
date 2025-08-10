package com.sam.vehicle_management_system.service;

import com.sam.vehicle_management_system.models.EFuelStatus;
import com.sam.vehicle_management_system.models.FuelRecord;
import com.sam.vehicle_management_system.payload.response.FuelRecordDto;
import com.sam.vehicle_management_system.payload.response.FuelReportDto; // <-- Import ใหม่
import com.sam.vehicle_management_system.repository.FuelRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate; // <-- Import ใหม่
import java.time.LocalTime; // <-- Import ใหม่
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FuelService {

    @Autowired
    private FuelRecordRepository fuelRecordRepository;

    @Transactional(readOnly = true)
    public List<FuelRecordDto> getPendingClearanceRecordsForUser(Long userId) {
        List<FuelRecord> records = fuelRecordRepository.findByStatusAndRecordedByIdWithDetails(EFuelStatus.PENDING_CLEARANCE, userId);
        return records.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // --- เพิ่มเมธอดนี้เข้ามาใหม่ ---
    @Transactional(readOnly = true)
    public FuelReportDto getFuelReport(LocalDate startDate, LocalDate endDate, Long vehicleId) {
        List<FuelRecord> records = fuelRecordRepository.findAllWithDetailsByFilters(
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX),
                vehicleId
        );
        List<FuelRecordDto> dtos = records.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new FuelReportDto(dtos);
    }
    // ----------------------------

    private FuelRecordDto convertToDto(FuelRecord record) {
        FuelRecordDto dto = new FuelRecordDto();
        dto.setId(record.getId());
        dto.setRecordTimestamp(record.getRecordTimestamp());
        dto.setMileageAtRefuel(record.getMileageAtRefuel());
        dto.setAmountPaid(record.getAmountPaid());
        dto.setReceiptImageUrl(record.getReceiptImageUrl());
        dto.setStatus(record.getStatus());
        dto.setProject(record.getProject());
        dto.setServiceProvider(record.getServiceProvider());
        dto.setAmountWithdrawn(record.getAmountWithdrawn());

        if (record.getRecordedBy() != null) {
            dto.setRecordedByFirstName(record.getRecordedBy().getFirstName());
            dto.setRecordedByLastName(record.getRecordedBy().getLastName());
        }

        if (record.getTrip() != null) {
            dto.setTripId(record.getTrip().getId());
            if (record.getTrip().getVehicle() != null) {
                dto.setVehicleName(record.getTrip().getVehicle().getName());
                dto.setLicensePlate(record.getTrip().getVehicle().getLicensePlate());
            }
        }

        return dto;
    }
}
