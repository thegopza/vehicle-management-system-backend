// path: src/main/java/com/sam/vehicle_management_system/payload/request/SignupRequest.java
package com.sam.vehicle_management_system.payload.request;

import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class SignupRequest {
    private String username;

    // --- ส่วนที่แก้ไข ---
    private String firstName;
    private String lastName;
    // -------------------

    private String password;
    private Set<String> role;
}