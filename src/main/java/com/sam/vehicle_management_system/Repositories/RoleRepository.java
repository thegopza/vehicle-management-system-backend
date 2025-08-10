// path: src/main/java/com/sam/vehicle_management_system/repository/RoleRepository.java
package com.sam.vehicle_management_system.repository;

import com.sam.vehicle_management_system.models.ERole;
import com.sam.vehicle_management_system.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}