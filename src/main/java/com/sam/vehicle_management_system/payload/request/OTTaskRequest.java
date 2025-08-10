package com.sam.vehicle_management_system.payload.request;

import lombok.Data;

@Data
public class OTTaskRequest {
    private Long checkpointId;
    private Long laneId;
    private Long equipmentId;
    private String customRepairItem;
    private String customFixDescription;
}