package com.sam.vehicle_management_system.controller;

import com.sam.vehicle_management_system.models.User;
import com.sam.vehicle_management_system.payload.response.NotificationDto;
import com.sam.vehicle_management_system.repository.UserRepository;
import com.sam.vehicle_management_system.security.services.UserDetailsImpl;
import com.sam.vehicle_management_system.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    // --- *** START: ส่วนที่แก้ไข *** ---
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications() {
        List<NotificationDto> notifications = notificationService.getUnreadNotificationsForUser(getCurrentUser());
        return ResponseEntity.ok(notifications);
    }
    // --- *** END: ส่วนที่แก้ไข *** ---

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = notificationService.getUnreadNotificationCount(getCurrentUser());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}