package com.sam.vehicle_management_system.repository;

import com.sam.vehicle_management_system.models.OTAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OTAttachmentRepository extends JpaRepository<OTAttachment, Long> {
    void deleteByOtRequestId(Long otRequestId);
}
