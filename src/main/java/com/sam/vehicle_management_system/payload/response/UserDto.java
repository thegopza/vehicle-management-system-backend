package com.sam.vehicle_management_system.payload.response;

import com.sam.vehicle_management_system.models.OTSystemMode; // <-- *** เพิ่ม Import ***
import lombok.Data;
import java.util.Set;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private Boolean active;
    private Set<String> roles;

    private OTSystemMode otSystemMode; // <-- *** เพิ่ม Field นี้เข้ามา ***
}