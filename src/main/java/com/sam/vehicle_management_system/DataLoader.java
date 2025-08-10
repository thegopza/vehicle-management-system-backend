// path: src/main/java/com/sam/vehicle_management_system/DataLoader.java
package com.sam.vehicle_management_system;

import com.sam.vehicle_management_system.models.ERole;
import com.sam.vehicle_management_system.models.Role;
import com.sam.vehicle_management_system.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_USER));
        }
        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
        }
        if (roleRepository.findByName(ERole.ROLE_MANAGER).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_MANAGER));
        }
        if (roleRepository.findByName(ERole.ROLE_KEY_RETURNER).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_KEY_RETURNER));
        }
        if (roleRepository.findByName(ERole.ROLE_CAO).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_CAO));
        }
    }
}