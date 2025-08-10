package com.sam.vehicle_management_system.payload.request;

import lombok.Data;
import java.util.Set;

@Data
public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    private Set<String> roles;
    private Boolean active;
}