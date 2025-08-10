package com.sam.vehicle_management_system.controller;

import com.sam.vehicle_management_system.models.ERole;
import com.sam.vehicle_management_system.models.Role;
import com.sam.vehicle_management_system.models.User;
import com.sam.vehicle_management_system.payload.request.CreateUserRequest;
import com.sam.vehicle_management_system.payload.request.UserUpdateRequest;
import com.sam.vehicle_management_system.payload.response.MessageResponse;
import com.sam.vehicle_management_system.payload.response.UserDto;
import com.sam.vehicle_management_system.repository.RoleRepository;
import com.sam.vehicle_management_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @GetMapping("/role/{roleName}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserDto>> getUsersByRole(@PathVariable String roleName) {
        // แปลง String เป็น ERole enum, ทำให้เป็นตัวพิมพ์ใหญ่ทั้งหมดเพื่อให้ตรงกับ ERole
        ERole roleEnum = ERole.valueOf("ROLE_" + roleName.toUpperCase());

        Role role = roleRepository.findByName(roleEnum)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        List<User> users = userRepository.findAllByRolesContaining(role);

        List<UserDto> userDtos = users.stream().map(this::convertToDto).collect(Collectors.toList());

        return ResponseEntity.ok(userDtos);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()") // เปลี่ยนจากเดิมที่จำกัดสิทธิ์แค่ ADMIN และ CAO
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAO')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest userUpdateRequest) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User is not found."));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isCurrentUserCao = isUserCao(authentication);

        boolean isTargetCao = targetUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals(ERole.ROLE_CAO));

        // --- Logic ป้องกัน ---
        // 1. Admin ทั่วไปห้ามแก้ไข CAO
        if (isTargetCao && !isCurrentUserCao) {
            return ResponseEntity.status(403).body(new MessageResponse("Error: Admins cannot modify CAO users."));
        }
        // 2. Admin ทั่วไปห้ามตั้ง Role CAO ให้ใคร
        if (userUpdateRequest.getRoles().contains("cao") && !isCurrentUserCao) {
            return ResponseEntity.status(403).body(new MessageResponse("Error: Only CAO can assign CAO role."));
        }
        // --------------------

        targetUser.setFirstName(userUpdateRequest.getFirstName());
        targetUser.setLastName(userUpdateRequest.getLastName());
        targetUser.setActive(userUpdateRequest.getActive());

        Set<String> strRoles = userUpdateRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles != null) {
            strRoles.forEach(role -> {
                switch (role) {
                    case "cao": roles.add(findRole(ERole.ROLE_CAO)); break;
                    case "admin": roles.add(findRole(ERole.ROLE_ADMIN)); break;
                    case "manager": roles.add(findRole(ERole.ROLE_MANAGER)); break;
                    case "key_returner": roles.add(findRole(ERole.ROLE_KEY_RETURNER)); break;
                    default: roles.add(findRole(ERole.ROLE_USER));
                }
            });
            targetUser.setRoles(roles);
        }

        userRepository.save(targetUser);
        return ResponseEntity.ok(new MessageResponse("User updated successfully!"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('CAO')")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest createUserRequest) {
        if (userRepository.existsByUsername(createUserRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        // --- Logic ป้องกัน ---
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (createUserRequest.getRoles() != null && createUserRequest.getRoles().contains("cao") && !isUserCao(authentication)) {
            return ResponseEntity.status(403).body(new MessageResponse("Error: Only CAO can create CAO users."));
        }
        // --------------------

        User user = new User(
                createUserRequest.getUsername(),
                createUserRequest.getFirstName(),
                createUserRequest.getLastName(),
                encoder.encode(createUserRequest.getPassword())
        );

        Set<String> strRoles = createUserRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            roles.add(findRole(ERole.ROLE_USER));
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "cao": roles.add(findRole(ERole.ROLE_CAO)); break;
                    case "admin": roles.add(findRole(ERole.ROLE_ADMIN)); break;
                    case "manager": roles.add(findRole(ERole.ROLE_MANAGER)); break;
                    case "key_returner": roles.add(findRole(ERole.ROLE_KEY_RETURNER)); break;
                    default: roles.add(findRole(ERole.ROLE_USER));
                }
            });
        }
        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    private UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());

        if (user.getActive() != null) {
            userDto.setActive(user.getActive());
        } else {
            userDto.setActive(false);
        }

        userDto.setOtSystemMode(user.getOtSystemMode());

        userDto.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name().replace("ROLE_", "").toLowerCase())
                .collect(Collectors.toSet()));
        return userDto;
    }

    private Role findRole(ERole roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
    }

    private boolean isUserCao(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(ERole.ROLE_CAO.name()));
    }
}
