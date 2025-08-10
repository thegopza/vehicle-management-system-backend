package com.sam.vehicle_management_system.payload.request;

import lombok.Data;
import java.util.Set;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private Set<String> roles;
}