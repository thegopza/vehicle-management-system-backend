package com.sam.vehicle_management_system.repository;

import com.sam.vehicle_management_system.models.OTTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OTTaskRepository extends JpaRepository<OTTask, Long> {

    List<OTTask> findAllByCheckpointId(Long checkpointId);

    List<OTTask> findAllByLaneId(Long laneId);

    List<OTTask> findAllByEquipmentId(Long equipmentId);

}