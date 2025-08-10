package com.sam.vehicle_management_system.payload.request;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
public class OTRequest {
    private Long managerId;
    private Set<Long> coworkerIds;
    private List<LocalDate> workDates;
    private LocalTime startTime;
    private LocalTime endTime;

    // General Mode Fields
    private String workLocation;
    private String project;
    private String reason;

    // Team Mode Fields
    private List<OTTaskRequest> tasks;

    // --- *** START: ส่วนที่เพิ่มเข้ามาใหม่ *** ---
    // Field for approver edits
    private String editNotes;
    // --- *** END: ส่วนที่เพิ่มเข้ามาใหม่ *** ---
}
