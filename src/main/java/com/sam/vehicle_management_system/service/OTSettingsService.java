package com.sam.vehicle_management_system.service;

import com.sam.vehicle_management_system.models.*;
import com.sam.vehicle_management_system.payload.response.CheckpointDto;
import com.sam.vehicle_management_system.payload.response.EquipmentDto;
import com.sam.vehicle_management_system.payload.response.LaneDto;
import com.sam.vehicle_management_system.payload.response.UserDto;
import com.sam.vehicle_management_system.repository.*;
import com.sam.vehicle_management_system.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OTSettingsService {

    @Autowired private UserRepository userRepository;
    @Autowired private CheckpointRepository checkpointRepository;
    @Autowired private LaneRepository laneRepository;
    @Autowired private EquipmentRepository equipmentRepository;
    @Autowired private OTTaskRepository otTaskRepository;

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: Current user not found."));
    }

    private boolean isManagerOrCao(User user) {
        return user.getRoles().stream()
                .anyMatch(r -> r.getName() == ERole.ROLE_MANAGER || r.getName() == ERole.ROLE_CAO);
    }

    private boolean canManageConditions(User user) {
        if (isManagerOrCao(user)) {
            return true;
        }
        return !userRepository.findManagersByAssistant(user).isEmpty();
    }

    // --- ส่วนการตั้งค่าส่วนตัว (สำหรับ Manager/CAO) ---

    @Transactional
    public void setOtModeForCurrentUser(OTSystemMode mode) {
        User currentUser = getCurrentUser();
        currentUser.setOtSystemMode(mode);
        userRepository.save(currentUser);
    }

    @Transactional
    public void addAssistant(Long assistantId) {
        User manager = getCurrentUser();
        User assistant = userRepository.findById(assistantId)
                .orElseThrow(() -> new RuntimeException("Error: Assistant user not found."));
        manager.getAssistants().add(assistant);
        userRepository.save(manager);
    }

    @Transactional
    public void removeAssistant(Long assistantId) {
        User manager = getCurrentUser();
        manager.getAssistants().removeIf(assistant -> assistant.getId().equals(assistantId));
        userRepository.save(manager);
    }

    @Transactional(readOnly = true)
    public Set<UserDto> getAssistants() {
        User manager = getCurrentUser();
        return manager.getAssistants().stream().map(this::convertToUserDto).collect(Collectors.toSet());
    }

    // --- ส่วนการจัดการด่านและเลน ---

    @Transactional
    public CheckpointDto createCheckpoint(String name) {
        User currentUser = getCurrentUser();
        // *** จุดที่แก้ไข: เปลี่ยนจาก isManagerOrCao เป็น canManageConditions ***
        // เพื่อให้ Assistant สามารถสร้าง Checkpoint ของ Manager ที่ตนเองสังกัดได้
        if (!canManageConditions(currentUser)) {
            throw new AccessDeniedException("Only Managers, CAO, or their Assistants can create new checkpoints.");
        }

        // กรณีเป็น Assistant ให้ดึง Manager คนแรกที่ตนเองสังกัดมาเป็นเจ้าของ Checkpoint
        User owner = isManagerOrCao(currentUser) ? currentUser : userRepository.findManagersByAssistant(currentUser).get(0);

        Checkpoint checkpoint = new Checkpoint();
        checkpoint.setName(name);
        checkpoint.setManager(owner); // กำหนด Manager เจ้าของ
        Checkpoint savedCheckpoint = checkpointRepository.save(checkpoint);
        return convertToCheckpointDto(savedCheckpoint);
    }

    @Transactional(readOnly = true)
    public List<CheckpointDto> getCheckpointsForCurrentUser() {
        User currentUser = getCurrentUser();
        if (!canManageConditions(currentUser)) {
            // กรณีไม่มีสิทธิ์เลย ให้ trả về รายการว่างแทนการโยน Exception
            return List.of();
        }

        if (isManagerOrCao(currentUser)) {
            return checkpointRepository.findAll().stream()
                    .filter(c -> c.getManager().getId().equals(currentUser.getId()))
                    .map(this::convertToCheckpointDto)
                    .sorted(Comparator.comparing(CheckpointDto::getName))
                    .collect(Collectors.toList());
        } else {
            List<User> managers = userRepository.findManagersByAssistant(currentUser);
            return checkpointRepository.findAll().stream()
                    .filter(c -> managers.stream().anyMatch(m -> m.getId().equals(c.getManager().getId())))
                    .map(this::convertToCheckpointDto)
                    .sorted(Comparator.comparing(CheckpointDto::getName))
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    public void deleteCheckpoint(Long checkpointId) {
        User currentUser = getCurrentUser();
        if (!canManageConditions(currentUser)) {
            throw new AccessDeniedException("User is not authorized to delete this checkpoint.");
        }

        Checkpoint checkpoint = checkpointRepository.findById(checkpointId)
                .orElseThrow(() -> new RuntimeException("Checkpoint not found with id: " + checkpointId));

        List<OTTask> tasksReferencingCheckpoint = otTaskRepository.findAllByCheckpointId(checkpointId);
        tasksReferencingCheckpoint.forEach(task -> task.setCheckpoint(null));
        otTaskRepository.saveAll(tasksReferencingCheckpoint);

        List<Lane> lanesToDelete = laneRepository.findAllByCheckpointId(checkpointId);
        lanesToDelete.forEach(lane -> deleteLane(lane.getId()));

        checkpointRepository.delete(checkpoint);
    }

    @Transactional
    public LaneDto createLane(Long checkpointId, String name) {
        User currentUser = getCurrentUser();
        if (!canManageConditions(currentUser)) {
            throw new AccessDeniedException("User is not authorized to create a lane.");
        }
        Checkpoint checkpoint = checkpointRepository.findById(checkpointId)
                .orElseThrow(() -> new RuntimeException("Checkpoint not found"));
        Lane lane = new Lane();
        lane.setName(name);
        lane.setCheckpoint(checkpoint);
        Lane savedLane = laneRepository.save(lane);
        return convertToLaneDto(savedLane);
    }

    @Transactional
    public void deleteLane(Long laneId) {
        User currentUser = getCurrentUser();
        if (!canManageConditions(currentUser)) {
            throw new AccessDeniedException("User is not authorized to delete a lane.");
        }

        Lane lane = laneRepository.findById(laneId).orElseThrow(() -> new RuntimeException("Lane not found"));

        List<OTTask> tasksReferencingLane = otTaskRepository.findAllByLaneId(laneId);
        tasksReferencingLane.forEach(task -> task.setLane(null));
        otTaskRepository.saveAll(tasksReferencingLane);

        laneRepository.delete(lane);
    }

    // --- ส่วนการจัดการอุปกรณ์ (Global) ---

    @Transactional(readOnly = true)
    public List<EquipmentDto> getAllEquipments() {
        return equipmentRepository.findAll().stream()
                .map(this::convertToEquipmentDto)
                .sorted(Comparator.comparing(EquipmentDto::getName))
                .collect(Collectors.toList());
    }

    @Transactional
    public EquipmentDto createEquipment(String name) {
        User currentUser = getCurrentUser();
        if (!canManageConditions(currentUser)) {
            throw new AccessDeniedException("User is not authorized to create equipment.");
        }
        Equipment equipment = new Equipment();
        equipment.setName(name);
        Equipment savedEquipment = equipmentRepository.save(equipment);
        return convertToEquipmentDto(savedEquipment);
    }

    @Transactional
    public void deleteEquipment(Long equipmentId) {
        User currentUser = getCurrentUser();
        if (!canManageConditions(currentUser)) {
            throw new AccessDeniedException("User is not authorized to delete equipment.");
        }

        List<OTTask> tasksReferencingEquipment = otTaskRepository.findAllByEquipmentId(equipmentId);
        tasksReferencingEquipment.forEach(task -> task.setEquipment(null));
        otTaskRepository.saveAll(tasksReferencingEquipment);

        equipmentRepository.deleteById(equipmentId);
    }

    // --- DTO Converters ---

    private UserDto convertToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setActive(user.getActive());
        userDto.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name().replace("ROLE_", "").toLowerCase())
                .collect(Collectors.toSet()));
        return userDto;
    }

    private LaneDto convertToLaneDto(Lane lane) {
        LaneDto dto = new LaneDto();
        dto.setId(lane.getId());
        dto.setName(lane.getName());
        return dto;
    }

    private EquipmentDto convertToEquipmentDto(Equipment equipment) {
        EquipmentDto dto = new EquipmentDto();
        dto.setId(equipment.getId());
        dto.setName(equipment.getName());
        return dto;
    }

    private CheckpointDto convertToCheckpointDto(Checkpoint checkpoint) {
        CheckpointDto dto = new CheckpointDto();
        dto.setId(checkpoint.getId());
        dto.setName(checkpoint.getName());

        List<LaneDto> lanes = laneRepository.findAllByCheckpointId(checkpoint.getId()).stream()
                .map(this::convertToLaneDto)
                .sorted(Comparator.comparing(LaneDto::getName))
                .collect(Collectors.toList());
        dto.setLanes(lanes);

        return dto;
    }
}