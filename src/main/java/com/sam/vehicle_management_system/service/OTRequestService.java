package com.sam.vehicle_management_system.service;

import com.sam.vehicle_management_system.models.*;
import com.sam.vehicle_management_system.payload.response.OTRequestDto;
import com.sam.vehicle_management_system.repository.*;
import com.sam.vehicle_management_system.security.services.UserDetailsImpl;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OTRequestService {

    @Autowired private UserRepository userRepository;
    @Autowired private OTRequestRepository otRequestRepository;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private CheckpointRepository checkpointRepository;
    @Autowired private LaneRepository laneRepository;
    @Autowired private EquipmentRepository equipmentRepository;
    @Autowired private OTApprovalService otApprovalService;
    @Autowired private OTAttachmentRepository otAttachmentRepository;

    // --- *** START: ส่วนที่เพิ่มเข้ามา *** ---
    @Autowired private NotificationService notificationService;
    // --- *** END: ส่วนที่เพิ่มเข้ามา *** ---


    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    @Transactional
    public OTRequest createOtRequest(com.sam.vehicle_management_system.payload.request.OTRequest requestPayload, List<MultipartFile> files) {
        User requester = getCurrentUser();
        User manager = userRepository.findById(requestPayload.getManagerId())
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        OTRequest newRequest = new OTRequest();
        newRequest.setRequester(requester);

        mapPayloadToEntity(newRequest, requestPayload, manager, false);

        if (files != null && !files.isEmpty()) {
            Set<OTAttachment> attachments = files.stream().map(file -> {
                String fileName = fileStorageService.storeFile(file);
                return new OTAttachment(null, fileName, fileName, fileName, file.getSize(), newRequest);
            }).collect(Collectors.toSet());
            newRequest.setAttachments(attachments);
        }

        OTRequest savedRequest = otRequestRepository.save(newRequest);

        // --- *** START: ส่วนที่เพิ่มเข้ามา *** ---
        // สร้าง Notification ส่งไปให้ Manager
        String notificationMessage = String.format("คุณมีคำขอ OT ใหม่จาก: %s %s",
                requester.getFirstName(), requester.getLastName());

        notificationService.createNotification(
                manager,
                requester,
                notificationMessage,
                "/ot/approve", // Link ไปยังหน้าอนุมัติ OT
                ENotificationType.OT_REQUEST
        );
        // --- *** END: ส่วนที่เพิ่มเข้ามา *** ---

        return savedRequest;
    }

    @Transactional
    public OTRequestDto getRequestById(Long id) {
        OTRequest request = otRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OT Request not found with id: " + id));
        return otApprovalService.convertToDto(request);
    }

    @Transactional
    public OTRequestDto updateOtRequest(Long id, com.sam.vehicle_management_system.payload.request.OTRequest requestPayload, List<MultipartFile> files) {
        OTRequest existingRequest = otRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OT Request not found with id: " + id));

        User manager = userRepository.findById(requestPayload.getManagerId())
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        mapPayloadToEntity(existingRequest, requestPayload, manager, true);

        existingRequest.getAttachments().forEach(attachment -> fileStorageService.deleteFile(attachment.getFileName()));
        existingRequest.getAttachments().clear();

        if (files != null && !files.isEmpty()) {
            Set<OTAttachment> newAttachments = files.stream().map(file -> {
                String fileName = fileStorageService.storeFile(file);
                return new OTAttachment(null, fileName, fileName, fileName, file.getSize(), existingRequest);
            }).collect(Collectors.toSet());
            existingRequest.getAttachments().addAll(newAttachments);
        }

        OTRequest updatedRequest = otRequestRepository.save(existingRequest);
        return otApprovalService.convertToDto(updatedRequest);
    }

    private void mapPayloadToEntity(OTRequest entity, com.sam.vehicle_management_system.payload.request.OTRequest payload, User manager, boolean isUpdate) {
        entity.setManager(manager);

        if (payload.getCoworkerIds() != null) {
            entity.setCoworkers(new HashSet<>(userRepository.findAllById(payload.getCoworkerIds())));
        }

        entity.setStartTime(payload.getStartTime());
        entity.setEndTime(payload.getEndTime());
        entity.setCalculatedHours((double) Duration.between(payload.getStartTime(), payload.getEndTime()).toMinutes() / 60);

        entity.getOtDates().clear();
        if (payload.getWorkDates() != null) {
            Set<OTDate> newDates = payload.getWorkDates().stream().map(date -> {
                OTDate otDate = new OTDate();
                otDate.setWorkDate(date);
                otDate.setOtRequest(entity);
                return otDate;
            }).collect(Collectors.toSet());
            entity.getOtDates().addAll(newDates);
        }

        entity.getOtTasks().clear();
        if (payload.getTasks() != null && !payload.getTasks().isEmpty()) {
            Set<OTTask> newTasks = payload.getTasks().stream().map(taskDto -> {
                OTTask task = new OTTask();
                if (taskDto.getCheckpointId() != null) task.setCheckpoint(checkpointRepository.findById(taskDto.getCheckpointId()).orElse(null));
                if (taskDto.getLaneId() != null) task.setLane(laneRepository.findById(taskDto.getLaneId()).orElse(null));
                if (taskDto.getEquipmentId() != null) task.setEquipment(equipmentRepository.findById(taskDto.getEquipmentId()).orElse(null));
                task.setCustomRepairItem(taskDto.getCustomRepairItem());
                task.setCustomFixDescription(taskDto.getCustomFixDescription());
                task.setOtRequest(entity);
                return task;
            }).collect(Collectors.toSet());
            entity.getOtTasks().addAll(newTasks);
        }

        entity.setEditNotes(payload.getEditNotes());

        if (isUpdate) {
            entity.setRejectionReason(null);
            if (payload.getEditNotes() != null && !payload.getEditNotes().isBlank()) {
                entity.setStatus(OTStatus.EDITED);
            } else {
                if (manager.getOtSystemMode() == OTSystemMode.TEAM) {
                    entity.setStatus(OTStatus.PENDING_ASSISTANT_REVIEW);
                } else {
                    entity.setStatus(OTStatus.PENDING_MANAGER_APPROVAL);
                }
            }
        } else {
            if (manager.getOtSystemMode() == OTSystemMode.TEAM) {
                entity.setStatus(OTStatus.PENDING_ASSISTANT_REVIEW);
            } else {
                entity.setStatus(OTStatus.PENDING_MANAGER_APPROVAL);
            }
        }

        entity.setWorkLocation(payload.getWorkLocation());
        entity.setProject(payload.getProject());
        entity.setReason(payload.getReason());
    }
}