// path: src/main/java/com/sam/vehicle_management_system/controller/AuthController.java
package com.sam.vehicle_management_system.controller;

import com.sam.vehicle_management_system.models.ERole;
import com.sam.vehicle_management_system.models.Role;
import com.sam.vehicle_management_system.models.User;
import com.sam.vehicle_management_system.payload.request.LoginRequest;
import com.sam.vehicle_management_system.payload.request.SignupRequest;
import com.sam.vehicle_management_system.payload.response.JwtResponse;
import com.sam.vehicle_management_system.payload.response.MessageResponse;
import com.sam.vehicle_management_system.repository.RoleRepository;
import com.sam.vehicle_management_system.repository.UserRepository;
import com.sam.vehicle_management_system.security.jwt.JwtUtils;
import com.sam.vehicle_management_system.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // --- ส่วนที่แก้ไข ---
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getFirstName(), // เปลี่ยนจาก getName()
                userDetails.getLastName(),  // เพิ่มเข้ามา
                roles));
        // -------------------
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        // --- ส่วนที่แก้ไข ---
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getFirstName(), // เปลี่ยนจาก getName()
                signUpRequest.getLastName(),  // เพิ่มเข้ามา
                encoder.encode(signUpRequest.getPassword()));
        // -------------------

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "cao":
                        Role caoRole = roleRepository.findByName(ERole.ROLE_CAO)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(caoRole);
                        break;
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "manager":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MANAGER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);
                        break;
                    case "key_returner":
                        Role keyReturnerRole = roleRepository.findByName(ERole.ROLE_KEY_RETURNER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(keyReturnerRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}