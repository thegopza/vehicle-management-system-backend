package com.sam.vehicle_management_system.models;

public enum OTStatus {
    PENDING_ASSISTANT_REVIEW,   // รอผู้ช่วยตรวจสอบ
    PENDING_MANAGER_APPROVAL,   // รอ Manager อนุมัติ
    APPROVED,                   // อนุมัติแล้ว
    REJECTED,                   // ปฏิเสธ
    EDITED                      // แก้ไขโดยผู้ตรวจสอบ
}