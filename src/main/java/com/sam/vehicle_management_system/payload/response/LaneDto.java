package com.sam.vehicle_management_system.payload.response;

import lombok.Data;
import java.util.List;

@Data
public class LaneDto {
    private Long id;
    private String name;
    // ไม่ต้องมี List<EquipmentDto> อีกต่อไป
}