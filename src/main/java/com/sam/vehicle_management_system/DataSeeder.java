package com.sam.vehicle_management_system;

import com.sam.vehicle_management_system.models.*;
import com.sam.vehicle_management_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * คลาสนี้ใช้สำหรับสร้างข้อมูลจำลองจำนวนมากลงในฐานข้อมูลเพื่อการทดสอบ
 * Script จะทำงานอัตโนมัติ 1 ครั้งเมื่อเริ่มต้นแอปพลิเคชัน
 * !! สำคัญ: หลังจากรันครั้งแรกแล้ว ควร Comment บรรทัด @Component ออกเพื่อไม่ให้ทำงานซ้ำ !!
 */
// @Component // <-- !! สำคัญ: คอมเมนต์บรรทัดนี้ออกหลังจากใช้งานเสร็จ !!
public class DataSeeder implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private TripRepository tripRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // --- ค่าที่สามารถปรับเปลี่ยนได้ ---
    private static final int NUM_USERS_TO_CREATE = 20;
    private static final int NUM_VEHICLES_TO_CREATE = 10;
    private static final int TRIPS_PER_VEHICLE = 100; // จำนวนการเดินทางจำลองต่อรถ 1 คัน
    // --------------------------------

    private final Random random = new Random();
    private final List<String> destinations = Arrays.asList("กรุงเทพ", "เชียงใหม่", "ภูเก็ต", "ขอนแก่น", "ระยอง", "หาดใหญ่", "พัทยา", "อุดรธานี");
    private final List<String> vehicleModels = Arrays.asList(
            "Toyota Vios", "Toyota Yaris", "Honda City", "Honda Civic", "Isuzu D-Max", "Toyota Revo", "Mitsubishi Triton", "Ford Ranger", "Nissan Almera", "Mazda 2"
    );

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("======================================================");
        System.out.println("========== STARTING DATA SEEDING PROCESS ==========");
        System.out.println("======================================================");

        // 1. สร้าง Roles (ถ้ายังไม่มี)
        Role userRole = roleRepository.findByName(ERole.ROLE_USER).orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_USER)));
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_ADMIN)));
        Role keyReturnerRole = roleRepository.findByName(ERole.ROLE_KEY_RETURNER).orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_KEY_RETURNER)));
        Role caoRole = roleRepository.findByName(ERole.ROLE_CAO).orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_CAO)));


        // 2. สร้าง Users
        List<User> users = createUsers(userRole);
        System.out.printf(">>>> Created %d new users.%n", users.size());

        // 3. สร้าง Vehicles
        List<Vehicle> vehicles = createVehicles();
        System.out.printf(">>>> Created %d new vehicles.%n", vehicles.size());

        // 4. สร้าง Trips, Fuel Records, and Accident Reports
        long totalTripsCreated = 0;
        for (Vehicle vehicle : vehicles) {
            createTripsForVehicle(vehicle, users);
            totalTripsCreated += TRIPS_PER_VEHICLE;
        }
        System.out.printf(">>>> Created %d new trips with random fuel/accident records.%n", totalTripsCreated);


        System.out.println("======================================================");
        System.out.println("============= DATA SEEDING PROCESS FINISHED =============");
        System.out.println("======================================================");
    }

    private List<User> createUsers(Role userRole) {
        List<User> userList = new ArrayList<>();
        if (userRepository.count() < NUM_USERS_TO_CREATE) { // สร้างเฉพาะเมื่อ user ยังไม่เยอะ
            for (int i = 1; i <= NUM_USERS_TO_CREATE; i++) {
                User user = new User();
                user.setUsername("user" + i);
                user.setPassword(passwordEncoder.encode("123456"));
                user.setFirstName("ผู้ใช้ทดสอบ" + i);
                user.setLastName("นามสกุล");
                user.setRoles(Collections.singleton(userRole));
                user.setActive(true);
                userList.add(user);
            }
            return userRepository.saveAll(userList);
        }
        return userRepository.findAll();
    }

    private List<Vehicle> createVehicles() {
        List<Vehicle> vehicleList = new ArrayList<>();
        if (vehicleRepository.count() < NUM_VEHICLES_TO_CREATE) {
            for (int i = 1; i <= NUM_VEHICLES_TO_CREATE; i++) {
                Vehicle vehicle = new Vehicle();
                vehicle.setName(vehicleModels.get(random.nextInt(vehicleModels.size())));
                vehicle.setLicensePlate(String.format("กข-%04d", i));
                vehicle.setAvailable(true);
                vehicle.setActive(true);
                vehicle.setLastMileage(random.nextInt(5000) + 1000); // สุ่มไมล์เริ่มต้น
                vehicle.setLastFuelLevel(String.valueOf(random.nextInt(3) + 6)); // 6-8
                vehicleList.add(vehicle);
            }
            return vehicleRepository.saveAll(vehicleList);
        }
        return vehicleRepository.findAll();
    }

    private void createTripsForVehicle(Vehicle vehicle, List<User> users) {
        int currentMileage = vehicle.getLastMileage();
        LocalDateTime lastTripEndTime = LocalDateTime.now().minusDays(1);

        for (int i = 0; i < TRIPS_PER_VEHICLE; i++) {
            Trip trip = new Trip();
            User randomUser = users.get(random.nextInt(users.size()));

            // --- Time Simulation ---
            long randomHoursAgo = (long) (i + 1) * 12 + random.nextInt(10); // เดินทางย้อนหลังไปเรื่อยๆ
            LocalDateTime startTime = LocalDateTime.now().minusHours(randomHoursAgo);
            LocalDateTime endTime = startTime.plusHours(random.nextInt(5) + 2); // เดินทาง 2-7 ชั่วโมง

            // --- Mileage Simulation ---
            int startMileage = currentMileage;
            int distance = 100 + random.nextInt(51); // 100-150 km
            int endMileage = startMileage + distance;
            currentMileage = endMileage;

            trip.setUser(randomUser);
            trip.setVehicle(vehicle);
            trip.setStartTime(startTime);
            trip.setEndTime(endTime);
            trip.setStartMileage(startMileage);
            trip.setEndMileage(endMileage);
            trip.setFuelLevel(String.valueOf(random.nextInt(4) + 1)); // 1-4
            trip.setDestination(destinations.get(random.nextInt(destinations.size())));
            trip.setStatus(Trip.TripStatus.COMPLETED);
            trip.setReturnedBy(randomUser);

            // --- Fuel Record Simulation (20% chance) ---
            if (random.nextDouble() < 0.20) {
                FuelRecord fuelRecord = new FuelRecord();
                fuelRecord.setTrip(trip);
                fuelRecord.setRecordedBy(randomUser);
                fuelRecord.setRecordTimestamp(startTime.plusMinutes(30));
                fuelRecord.setMileageAtRefuel(startMileage + random.nextInt(50));
                fuelRecord.setAmountPaid(BigDecimal.valueOf(random.nextInt(501) + 500)); // 500-1000 baht
                fuelRecord.setStatus(EFuelStatus.CLEARED); // สมมติว่าเคลียร์แล้ว

                List<FuelRecord> fuelRecords = new ArrayList<>();
                fuelRecords.add(fuelRecord);
                trip.setFuelRecords(fuelRecords);
            }

            // --- Accident Simulation (5% chance) ---
            if (random.nextDouble() < 0.05) {
                AccidentReport accidentReport = new AccidentReport();
                accidentReport.setTrip(trip);
                accidentReport.setAccidentTime(startTime.plusHours(1));
                accidentReport.setDescription("เกิดอุบัติเหตุเฉี่ยวชนเล็กน้อย");
                accidentReport.setLocation("ระหว่างทางไป " + trip.getDestination());
                trip.setAccidentReport(accidentReport);
            }

            tripRepository.save(trip);
        }

        // Update vehicle's final mileage
        vehicle.setLastMileage(currentMileage);
        vehicleRepository.save(vehicle);
    }
}
