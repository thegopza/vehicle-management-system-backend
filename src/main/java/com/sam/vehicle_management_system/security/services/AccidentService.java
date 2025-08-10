package com.sam.vehicle_management_system.service;

import com.sam.vehicle_management_system.models.AccidentReport;
import com.sam.vehicle_management_system.models.Trip;
import com.sam.vehicle_management_system.models.Vehicle;
import com.sam.vehicle_management_system.payload.response.AccidentReportDetailDto;
import com.sam.vehicle_management_system.payload.response.VehicleAccidentsDto;
import com.sam.vehicle_management_system.repository.AccidentReportRepository;
import com.sam.vehicle_management_system.repository.TripRepository; // <-- เพิ่ม Import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AccidentService {

    @Autowired
    private AccidentReportRepository accidentReportRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private TripRepository tripRepository; // <-- เพิ่ม Import

    public List<VehicleAccidentsDto> getGroupedAccidentReports() {
        List<AccidentReport> allReports = accidentReportRepository.findAllWithPhotosOrderByAccidentTimeDesc();

        List<AccidentReport> completedReports = allReports.stream()
                .filter(report -> report.getTrip().getStatus() == Trip.TripStatus.COMPLETED)
                .collect(Collectors.toList());

        Map<Vehicle, List<AccidentReport>> groupedByVehicle = completedReports.stream()
                .collect(Collectors.groupingBy(report -> report.getTrip().getVehicle()));

        return groupedByVehicle.entrySet().stream()
                .map(entry -> {
                    Vehicle vehicle = entry.getKey();
                    List<AccidentReport> reports = entry.getValue();

                    VehicleAccidentsDto vehicleDto = new VehicleAccidentsDto();
                    vehicleDto.setVehicleId(vehicle.getId());
                    vehicleDto.setVehicleName(vehicle.getName());
                    vehicleDto.setLicensePlate(vehicle.getLicensePlate());

                    List<AccidentReportDetailDto> reportDtos = reports.stream()
                            .map(this::convertToDetailDto)
                            .collect(Collectors.toList());

                    vehicleDto.setAccidentReports(reportDtos);
                    return vehicleDto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void clearAccidentReportsForVehicle(Long vehicleId) {
        // --- ส่วนที่แก้ไข: Logic การลบข้อมูลใหม่ทั้งหมด ---
        List<AccidentReport> reportsToDelete = accidentReportRepository.findAllByTripVehicleId(vehicleId);

        reportsToDelete.forEach(report -> {
            // 1. ลบไฟล์รูปภาพที่เกี่ยวข้อง
            if (report.getPhotoUrls() != null) {
                report.getPhotoUrls().forEach(fileStorageService::deleteFile);
            }
            // 2. ตัดความสัมพันธ์จาก Trip หลัก
            Trip parentTrip = report.getTrip();
            if (parentTrip != null) {
                parentTrip.setAccidentReport(null);
                tripRepository.save(parentTrip); // บันทึกการเปลี่ยนแปลงที่ Trip
            }
        });

        // 3. ลบรายงานอุบัติเหตุออกจากฐานข้อมูล
        accidentReportRepository.deleteAll(reportsToDelete);
        // ---------------------------------------------
    }

    private AccidentReportDetailDto convertToDetailDto(AccidentReport report) {
        AccidentReportDetailDto dto = new AccidentReportDetailDto();
        dto.setId(report.getId());
        dto.setTripId(report.getTrip().getId());
        dto.setAccidentTime(report.getAccidentTime());
        dto.setDescription(report.getDescription());
        dto.setLocation(report.getLocation());
        dto.setPhotoUrls(report.getPhotoUrls());

        if (report.getTrip().getReturnedBy() != null) {
            dto.setReporterFirstName(report.getTrip().getReturnedBy().getFirstName());
            dto.setReporterLastName(report.getTrip().getReturnedBy().getLastName());
        } else if (report.getTrip().getUser() != null) {
            dto.setReporterFirstName(report.getTrip().getUser().getFirstName());
            dto.setReporterLastName(report.getTrip().getUser().getLastName());
        }

        return dto;
    }
}
