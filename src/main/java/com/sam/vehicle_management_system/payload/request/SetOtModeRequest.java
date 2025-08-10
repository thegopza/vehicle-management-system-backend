package com.sam.vehicle_management_system.payload.request;

import com.sam.vehicle_management_system.models.OTSystemMode;
import lombok.Data;

@Data
public class SetOtModeRequest {
    private OTSystemMode mode;
}