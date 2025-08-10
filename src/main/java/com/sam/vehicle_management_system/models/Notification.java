package com.sam.vehicle_management_system.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient; // ผู้รับ

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender; // ผู้ส่ง (ถ้ามี)

    @Column(nullable = false)
    private String message; // ข้อความ

    @Column(name = "is_read", nullable = false)
    private boolean read = false; // สถานะ อ่าน/ยังไม่อ่าน

    @Column(nullable = false)
    private String link; // ลิงก์ที่จะพาไป

    @Enumerated(EnumType.STRING)
    private ENotificationType type; // ประเภท

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}