package com.sam.vehicle_management_system.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ot_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OTAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    // --- *** START: ส่วนที่เพิ่มเข้ามาใหม่ *** ---
    @Column(name = "file_url", nullable = false)
    private String fileUrl;
    // --- *** END: ส่วนที่เพิ่มเข้ามาใหม่ *** ---

    @Column(name = "size")
    private Long size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ot_request_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OTRequest otRequest;
}
