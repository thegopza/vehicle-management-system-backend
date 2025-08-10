package com.sam.vehicle_management_system.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sam.vehicle_management_system.models.OTStatus;
import lombok.Data;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
public class OTRequestDto {
    private Long id;
    private OTStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime endTime;

    private Double calculatedHours;
    private String workLocation;
    private String project;
    private String reason;
    private String rejectionReason;

    // --- *** START: ส่วนที่เพิ่มเข้ามาใหม่ *** ---
    private String editNotes; // เหตุผลการแก้ไข
    // --- *** END: ส่วนที่เพิ่มเข้ามาใหม่ *** ---

    private UserSimpleDto requester;
    private UserSimpleDto manager;
    private Set<UserSimpleDto> coworkers;
    private List<OTDateDto> otDates;
    private List<String> attachments;
    private List<OTTaskDto> tasks;
}
