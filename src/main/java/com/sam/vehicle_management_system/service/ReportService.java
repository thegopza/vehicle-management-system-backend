package com.sam.vehicle_management_system.service;

import com.sam.vehicle_management_system.models.Trip;
import com.sam.vehicle_management_system.models.User;
import com.sam.vehicle_management_system.payload.response.*;
import com.sam.vehicle_management_system.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private TripRepository tripRepository;

    @Transactional(readOnly = true)
    public MonthlyReportDto generateMonthlySummary(LocalDate startDate, LocalDate endDate) {
        List<Trip> trips = tripRepository.findAllCompletedBetweenDatesWithDetails(
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        );

        MonthlyReportDto monthlyReport = new MonthlyReportDto();
        if (trips.isEmpty()) {
            return monthlyReport;
        }

        Map<Long, List<Trip>> tripsByVehicle = trips.stream()
                .filter(t -> t.getVehicle() != null)
                .collect(Collectors.groupingBy(trip -> trip.getVehicle().getId()));

        List<VehicleReportDto> vehicleReports = tripsByVehicle.entrySet().stream().map(entry -> {
            List<Trip> vehicleTrips = entry.getValue();
            VehicleReportDto vehicleReport = new VehicleReportDto();

            vehicleReport.setVehicleId(vehicleTrips.get(0).getVehicle().getId());
            vehicleReport.setVehicleName(vehicleTrips.get(0).getVehicle().getName());
            vehicleReport.setLicensePlate(vehicleTrips.get(0).getVehicle().getLicensePlate());
            vehicleReport.setTotalTrips(vehicleTrips.size());

            int totalDistance = vehicleTrips.stream()
                    .mapToInt(trip -> (trip.getEndMileage() != null && trip.getStartMileage() != null) ? trip.getEndMileage() - trip.getStartMileage() : 0)
                    .sum();
            vehicleReport.setTotalDistance(totalDistance);

            Map<User, Integer> distanceByUser = vehicleTrips.stream()
                    .filter(t -> t.getUser() != null && t.getEndMileage() != null && t.getStartMileage() != null)
                    .collect(Collectors.groupingBy(Trip::getUser, Collectors.summingInt(t -> t.getEndMileage() - t.getStartMileage())));

            List<UserUsageDto> userUsageDetails = distanceByUser.entrySet().stream()
                    .map(e -> new UserUsageDto(e.getKey().getFirstName() + " " + e.getKey().getLastName(), e.getValue()))
                    .sorted(Comparator.comparingInt(UserUsageDto::getTotalDistance).reversed())
                    .collect(Collectors.toList());
            vehicleReport.setUserUsage(userUsageDetails);

            // --- ส่วนที่แก้ไข Logic การสรุปอุบัติเหตุ ---
            List<AccidentSummaryDto> accidentSummaries = vehicleTrips.stream()
                    .filter(trip -> trip.getAccidentReport() != null && trip.getUser() != null) // 1. กรอง Trip ที่มีอุบัติเหตุและมีผู้ใช้
                    .collect(Collectors.groupingBy(Trip::getUser, Collectors.counting()))      // 2. จัดกลุ่ม Trip ตามผู้ใช้แล้วนับจำนวน
                    .entrySet().stream()
                    .map(e -> new AccidentSummaryDto(e.getKey().getFirstName() + " " + e.getKey().getLastName(), e.getValue().intValue()))
                    .collect(Collectors.toList());
            vehicleReport.setAccidentSummary(accidentSummaries);
            // ---------------------------------------------

            List<FuelSummaryDto> fuelSummaries = vehicleTrips.stream()
                    .flatMap(t -> t.getFuelRecords().stream())
                    .filter(fr -> fr.getRecordedBy() != null)
                    .map(fr -> new FuelSummaryDto(fr.getRecordedBy().getFirstName() + " " + fr.getRecordedBy().getLastName(), fr.getRecordTimestamp().toLocalDate(), fr.getAmountPaid()))
                    .collect(Collectors.toList());
            vehicleReport.setFuelSummary(fuelSummaries);

            BigDecimal totalFuelCostForVehicle = fuelSummaries.stream()
                    .map(FuelSummaryDto::getAmountPaid)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            vehicleReport.setTotalFuelCost(totalFuelCostForVehicle);


            return vehicleReport;
        }).collect(Collectors.toList());

        monthlyReport.setVehicleReports(vehicleReports);

        OverallFuelSummaryDto overallSummary = new OverallFuelSummaryDto();
        BigDecimal totalFuelCostAllVehicles = vehicleReports.stream()
                .map(VehicleReportDto::getTotalFuelCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        overallSummary.setTotalFuelCost(totalFuelCostAllVehicles);

        Map<String, BigDecimal> totalFuelCostByVehicle = vehicleReports.stream()
                .collect(Collectors.toMap(VehicleReportDto::getLicensePlate, VehicleReportDto::getTotalFuelCost));
        overallSummary.setFuelCostByVehicle(totalFuelCostByVehicle);

        monthlyReport.setOverallFuelSummary(overallSummary);

        return monthlyReport;
    }
}
