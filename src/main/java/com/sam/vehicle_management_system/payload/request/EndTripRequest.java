package com.sam.vehicle_management_system.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EndTripRequest {
    private Integer endMileage;
    private String fuelLevel;
    private String notes; // บันทึกเพิ่มเติม
    private boolean hasAccident; // มีอุบัติเหตุหรือไม่

    // ข้อมูลสำหรับรายงานอุบัติเหตุ (ถ้ามี)
    private String accidentDescription;
    private String accidentLocation;
    // เราจะเพิ่มการอัปโหลดรูปในภายหลัง
}