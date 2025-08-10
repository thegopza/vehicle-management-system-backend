package com.sam.vehicle_management_system.repository;

import com.sam.vehicle_management_system.models.Role; // <-- Import
import com.sam.vehicle_management_system.models.User;
import org.springframework.data.jpa.repository.Query; // <-- Import
import org.springframework.data.repository.query.Param; // <-- Import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // <-- Import
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);

    // --- เพิ่มเมธอดนี้เข้าไป ---
    List<User> findAllByRolesContaining(Role role);

    @Query("SELECT m FROM User m WHERE :assistant MEMBER OF m.assistants")
    List<User> findManagersByAssistant(@Param("assistant") User assistant);
}