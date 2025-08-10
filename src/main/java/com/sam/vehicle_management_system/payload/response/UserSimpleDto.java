package com.sam.vehicle_management_system.payload.response;

import lombok.Data;

// DTO สำหรับเก็บข้อมูล User แบบย่อ
@Data
public class UserSimpleDto {
    private Long id;
    private String firstName;
    private String lastName;
}