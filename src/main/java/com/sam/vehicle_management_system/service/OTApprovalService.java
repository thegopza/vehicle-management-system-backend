package com.sam.vehicle_management_system.service;

import com.sam.vehicle_management_system.exception.OtConflictException;
import com.sam.vehicle_management_system.models.*;
import com.sam.vehicle_management_system.payload.response.OTDateDto;
import com.sam.vehicle_management_system.payload.response.OTRequestDto;
import com.sam.vehicle_management_system.payload.response.OTTaskDto;
import com.sam.vehicle_management_system.payload.response.UserSimpleDto;
import com.sam.vehicle_management_system.repository.OTRequestRepository;
import com.sam.vehicle_management_system.repository.UserRepository;
import com.sam.vehicle_management_system.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OTApprovalService {

    @Autowired private OTRequestRepository otRequestRepository;
    @Autowired private UserRepository userRepository;

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId()).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<OTRequestDto> getPendingRequests() {
        User currentUser = getCurrentUser();
        Set<String> roles = currentUser.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        List<OTRequest> requests = new ArrayList<>();

        if (roles.contains("ROLE_MANAGER") || roles.contains("ROLE_CAO")) {
            Set<OTStatus> managerStatuses = Set.of(OTStatus.PENDING_MANAGER_APPROVAL);
            requests.addAll(otRequestRepository.findByManagerAndStatusIn(currentUser, managerStatuses));
        }

        List<User> managersTheyAssist = userRepository.findManagersByAssistant(currentUser);
        if (!managersTheyAssist.isEmpty()) {
            requests.addAll(otRequestRepository.findByManagerInAndStatus(managersTheyAssist, OTStatus.PENDING_ASSISTANT_REVIEW));
        }

        return requests.stream()
                .distinct()
                .sorted(Comparator.comparing(OTRequest::getId).reversed())
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OTRequestDto approveRequest(Long requestId, boolean overwrite) {
        OTRequest requestToApprove = otRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("OT Request not found"));
        // Remainder of method is correct...
        User currentUser = getCurrentUser();
        List<User> involvedUsers = Stream.concat(
                Stream.of(requestToApprove.getRequester()),
                requestToApprove.getCoworkers().stream()
        ).distinct().collect(Collectors.toList());

        List<LocalDate> involvedDates = requestToApprove.getOtDates().stream()
                .map(OTDate::getWorkDate)
                .collect(Collectors.toList());

        if (!involvedDates.isEmpty()) {
            List<OTRequest> existingApproved = otRequestRepository.findExistingApprovedOtForUsersAndDates(involvedUsers, involvedDates);

            if (!existingApproved.isEmpty() && !overwrite) {
                String userNames = existingApproved.stream()
                        .flatMap(req -> Stream.concat(Stream.of(req.getRequester()), req.getCoworkers().stream()))
                        .filter(involvedUsers::contains)
                        .map(User::getFirstName)
                        .distinct()
                        .collect(Collectors.joining(", "));
                throw new OtConflictException("ตรวจพบ OT ที่อนุมัติแล้วของ: " + userNames + " ในวันดังกล่าว");
            }

            if (overwrite) {
                otRequestRepository.deleteAll(existingApproved);
            }
        }

        requestToApprove.setStatus(OTStatus.APPROVED);
        requestToApprove.setApprovedBy(currentUser);
        requestToApprove.setApprovedAt(LocalDateTime.now());
        OTRequest savedRequest = otRequestRepository.save(requestToApprove);
        return convertToDto(savedRequest);
    }

    @Transactional
    public OTRequestDto rejectRequest(Long requestId, String reason) {
        OTRequest request = otRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("OT Request not found"));
        request.setStatus(OTStatus.REJECTED);
        request.setRejectionReason(reason);
        OTRequest savedRequest = otRequestRepository.save(request);
        return convertToDto(savedRequest);
    }

    @Transactional
    public OTRequestDto assistantReview(Long requestId) {
        OTRequest request = otRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("OT Request not found"));
        request.setStatus(OTStatus.PENDING_MANAGER_APPROVAL);
        OTRequest savedRequest = otRequestRepository.save(request);
        return convertToDto(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<OTRequestDto> getMyOtHistory() {
        User currentUser = getCurrentUser();
        List<OTRequest> requests = otRequestRepository.findRequestsInvolvingUser(currentUser);
        return requests.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OTRequestDto> getOtSummary(LocalDate startDate, LocalDate endDate) {
        User currentUser = getCurrentUser();
        List<OTRequest> requests = otRequestRepository.findApprovedRequestsForUserInDateRange(currentUser, startDate, endDate);
        requests.sort(Comparator.comparing(OTRequest::getId));
        return requests.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public OTRequestDto convertToDto(OTRequest request) {
        OTRequestDto dto = new OTRequestDto();
        dto.setId(request.getId());
        dto.setStatus(request.getStatus());
        dto.setStartTime(request.getStartTime());
        dto.setEndTime(request.getEndTime());
        dto.setCalculatedHours(request.getCalculatedHours());
        dto.setWorkLocation(request.getWorkLocation());
        dto.setProject(request.getProject());
        dto.setReason(request.getReason());
        dto.setRejectionReason(request.getRejectionReason());
        dto.setEditNotes(request.getEditNotes());
        dto.setRequester(convertToUserSimpleDto(request.getRequester()));
        dto.setManager(convertToUserSimpleDto(request.getManager()));

        // --- *** START: ส่วนที่แก้ไข *** ---
        // Convert Set<OTAttachment> to List<String>
        if (request.getAttachments() != null) {
            dto.setAttachments(request.getAttachments().stream()
                    .map(OTAttachment::getFileName) // Or getFilePath, assuming they are the same
                    .collect(Collectors.toList()));
        }
        // --- *** END: ส่วนที่แก้ไข *** ---

        dto.setCoworkers(request.getCoworkers().stream()
                .map(this::convertToUserSimpleDto)
                .collect(Collectors.toSet()));

        dto.setOtDates(request.getOtDates().stream()
                .sorted(Comparator.comparing(OTDate::getWorkDate))
                .map(otDate -> {
                    OTDateDto dateDto = new OTDateDto();
                    dateDto.setId(otDate.getId());
                    dateDto.setWorkDate(otDate.getWorkDate());
                    return dateDto;
                })
                .collect(Collectors.toList()));

        if (request.getOtTasks() != null) {
            dto.setTasks(request.getOtTasks().stream()
                    .sorted(Comparator.comparing(OTTask::getId))
                    .map(task -> {
                        OTTaskDto taskDto = new OTTaskDto();
                        taskDto.setId(task.getId());
                        taskDto.setCustomRepairItem(task.getCustomRepairItem());
                        taskDto.setCustomFixDescription(task.getCustomFixDescription());
                        if (task.getCheckpoint() != null) {
                            taskDto.setCheckpointId(task.getCheckpoint().getId());
                            taskDto.setCheckpointName(task.getCheckpoint().getName());
                        }
                        if (task.getLane() != null) {
                            taskDto.setLaneId(task.getLane().getId());
                            taskDto.setLaneName(task.getLane().getName());
                        }
                        if (task.getEquipment() != null) {
                            taskDto.setEquipmentId(task.getEquipment().getId());
                            taskDto.setEquipmentName(task.getEquipment().getName());
                        }
                        return taskDto;
                    }).collect(Collectors.toList()));
        }
        return dto;
    }

    public UserSimpleDto convertToUserSimpleDto(User user) {
        if (user == null) return null;
        UserSimpleDto dto = new UserSimpleDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        return dto;
    }
}
