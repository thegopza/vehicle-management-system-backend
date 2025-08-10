// path: src/main/java/com/sam/vehicle_management_system/payload/response/JwtResponse.java
package com.sam.vehicle_management_system.payload.response;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;

    // --- ส่วนที่แก้ไข ---
    private String firstName;
    private String lastName;
    // -------------------

    private List<String> roles;

    // --- ส่วนที่แก้ไข (Constructor) ---
    public JwtResponse(String accessToken, Long id, String username, String firstName, String lastName, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
    }
    // ---------------------------------
}