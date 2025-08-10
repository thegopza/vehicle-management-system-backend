package com.sam.vehicle_management_system.repository;

import com.sam.vehicle_management_system.models.Notification;
import com.sam.vehicle_management_system.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // ค้นหาการแจ้งเตือนทั้งหมดของผู้รับ โดยเรียงจากใหม่ไปเก่า
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    // ค้นหาการแจ้งเตือนที่ยังไม่ได้อ่าน
    List<Notification> findByRecipientAndReadFalseOrderByCreatedAtDesc(User recipient);

    // นับจำนวนการแจ้งเตือนที่ยังไม่ได้อ่าน
    long countByRecipientAndReadFalse(User recipient);
}