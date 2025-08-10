package com.sam.vehicle_management_system.models;

import com.fasterxml.jackson.annotation.JsonIgnore; // <-- เพิ่ม Import
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String firstName;
    private String lastName;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    private Boolean active = true;

    // --- [OT SYSTEM] FIELD AND RELATIONSHIPS ADDED ---

    @Enumerated(EnumType.STRING)
    private OTSystemMode otSystemMode; // สำหรับ Manager: TEAM หรือ GENERAL

    // --- *** START: ส่วนที่แก้ไข *** ---
    @JsonIgnore // <-- เพิ่ม Annotation นี้
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "manager_assistants",
            joinColumns = @JoinColumn(name = "manager_id"),
            inverseJoinColumns = @JoinColumn(name = "assistant_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> assistants = new HashSet<>(); // ผู้ช่วยของ Manager
    // --- *** END: ส่วนที่แก้ไข *** ---

    public User(String username, String firstName, String lastName, String password) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
    }
}