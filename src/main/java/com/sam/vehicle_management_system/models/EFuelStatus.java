package com.sam.vehicle_management_system.models;

public enum EFuelStatus {
    RECORDED,           // บันทึกแล้ว แต่ Trip ยังไม่ถูกยืนยัน
    PENDING_CLEARANCE,  // รอเคลียร์บิล (Trip ถูกยืนยันแล้ว)
    CLEARED             // เคลียร์บิลแล้ว
}