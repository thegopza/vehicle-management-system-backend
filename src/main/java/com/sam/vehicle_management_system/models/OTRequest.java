package com.sam.vehicle_management_system.models;

import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ot_requests")
@Data
@NoArgsConstructor
@NamedEntityGraph(
        name = "ot-request-details-graph",
        attributeNodes = {
                @NamedAttributeNode("requester"),
                @NamedAttributeNode("manager"),
                @NamedAttributeNode("coworkers"),
                @NamedAttributeNode("otDates"),
                @NamedAttributeNode("otTasks"),
                @NamedAttributeNode("attachments") // Add attachments to the graph
        }
)
public class OTRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OTStatus status;

    private LocalTime startTime;
    private LocalTime endTime;
    private Double calculatedHours;

    private String workLocation;
    private String project;
    private String reason;

    private String rejectionReason;

    @Column(columnDefinition = "TEXT")
    private String editNotes;

    private java.time.LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "ot_request_coworkers",
            joinColumns = @JoinColumn(name = "ot_request_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> coworkers = new HashSet<>();

    @OneToMany(mappedBy = "otRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<OTDate> otDates = new HashSet<>();

    @OneToMany(mappedBy = "otRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<OTTask> otTasks = new HashSet<>();

    // --- *** START: ส่วนที่แก้ไข *** ---
    // Change from @ElementCollection to @OneToMany with the new OTAttachment entity
    @OneToMany(mappedBy = "otRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<OTAttachment> attachments = new HashSet<>();
    // --- *** END: ส่วนที่แก้ไข *** ---
}
