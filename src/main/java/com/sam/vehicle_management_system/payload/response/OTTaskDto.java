package com.sam.vehicle_management_system.payload.response;

import lombok.Data;

@Data
public class OTTaskDto {
    private Long id;
    private String customRepairItem;
    private String customFixDescription;
    private Long checkpointId;
    private String checkpointName;
    private Long laneId;
    private String laneName;
    private Long equipmentId;
    private String equipmentName;
}