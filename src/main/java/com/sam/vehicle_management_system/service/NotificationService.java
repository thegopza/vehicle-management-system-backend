package com.sam.vehicle_management_system.service;

import com.sam.vehicle_management_system.models.ENotificationType;
import com.sam.vehicle_management_system.models.Notification;
import com.sam.vehicle_management_system.models.User;
import com.sam.vehicle_management_system.payload.response.NotificationDto;
import com.sam.vehicle_management_system.payload.response.UserSimpleDto;
import com.sam.vehicle_management_system.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(User recipient, User sender, String message, String link, ENotificationType type) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setSender(sender);
        notification.setMessage(message);
        notification.setLink(link);
        notification.setType(type);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    // --- *** START: ส่วนที่แก้ไข *** ---
    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotificationsForUser(User user) {
        List<Notification> notifications = notificationRepository.findByRecipientAndReadFalseOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    // --- *** END: ส่วนที่แก้ไข *** ---

    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(User user) {
        return notificationRepository.countByRecipientAndReadFalse(user);
    }

    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    // --- *** START: ส่วนที่เพิ่มเข้ามาใหม่ *** ---
    // Helper method to convert Entity to DTO
    private NotificationDto convertToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setLink(notification.getLink());
        dto.setRead(notification.isRead());
        dto.setType(notification.getType());
        dto.setCreatedAt(notification.getCreatedAt());

        if (notification.getSender() != null) {
            UserSimpleDto senderDto = new UserSimpleDto();
            senderDto.setId(notification.getSender().getId());
            senderDto.setFirstName(notification.getSender().getFirstName());
            senderDto.setLastName(notification.getSender().getLastName());
            dto.setSender(senderDto);
        }

        return dto;
    }
    // --- *** END: ส่วนที่เพิ่มเข้ามาใหม่ *** ---
}