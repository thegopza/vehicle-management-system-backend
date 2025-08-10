package com.sam.vehicle_management_system.payload.response;

import lombok.Data;
import java.util.List;

@Data
public class CheckpointDto {
    private Long id;
    private String name;
    private List<LaneDto> lanes;
    // ลบ List<EquipmentDto> equipments; ออก
}