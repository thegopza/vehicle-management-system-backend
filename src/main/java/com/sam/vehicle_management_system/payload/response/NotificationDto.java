package com.sam.vehicle_management_system.payload.response;

import com.sam.vehicle_management_system.models.ENotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private Long id;
    private UserSimpleDto sender;
    private String message;
    private boolean read;
    private String link;
    private ENotificationType type;
    private LocalDateTime createdAt;
}