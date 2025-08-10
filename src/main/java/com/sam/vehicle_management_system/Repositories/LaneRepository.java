package com.sam.vehicle_management_system.repository;

import com.sam.vehicle_management_system.models.Lane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LaneRepository extends JpaRepository<Lane, Long> {
    List<Lane> findAllByCheckpointId(Long checkpointId);
}