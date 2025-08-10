package com.sam.vehicle_management_system.repository;

import com.sam.vehicle_management_system.models.OTRequest;
import com.sam.vehicle_management_system.models.OTStatus;
import com.sam.vehicle_management_system.models.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Collection; // <-- Import

@Repository
public interface OTRequestRepository extends JpaRepository<OTRequest, Long> {

    // --- *** นี่คือเวอร์ชันที่แก้ไข Query ให้ถูกต้องสมบูรณ์ *** ---

    @Query("SELECT DISTINCT r FROM OTRequest r " +
            "LEFT JOIN FETCH r.requester " +
            "LEFT JOIN FETCH r.manager " +
            "LEFT JOIN FETCH r.coworkers " +
            "LEFT JOIN FETCH r.otDates " +
            "WHERE r.manager = :manager AND r.status IN :statuses")
    List<OTRequest> findByManagerAndStatusIn(@Param("manager") User manager, @Param("statuses") Set<OTStatus> statuses);

    @Query("SELECT DISTINCT r FROM OTRequest r " +
            "LEFT JOIN FETCH r.requester " +
            "LEFT JOIN FETCH r.manager " +
            "LEFT JOIN FETCH r.coworkers " +
            "LEFT JOIN FETCH r.otDates " +
            "WHERE r.manager IN :managers AND r.status = :status")
    List<OTRequest> findByManagerInAndStatus(@Param("managers") List<User> managers, @Param("status") OTStatus status);


    @Query("SELECT r FROM OTRequest r JOIN r.otDates d " +
            "WHERE (r.requester IN :users OR EXISTS (SELECT c FROM r.coworkers c WHERE c IN :users)) " +
            "AND d.workDate IN :dates AND r.status = 'APPROVED'")
    List<OTRequest> findExistingApprovedOtForUsersAndDates(
            @Param("users") Collection<User> users,
            @Param("dates") Collection<LocalDate> dates
    );
    // --------------------------------------------------------

    @Query("SELECT r FROM OTRequest r LEFT JOIN r.coworkers c WHERE r.requester = :user OR c = :user ORDER BY r.id DESC")
    @EntityGraph(value = "ot-request-details-graph")
    List<OTRequest> findRequestsInvolvingUser(@Param("user") User user);

    @Query("SELECT DISTINCT r FROM OTRequest r LEFT JOIN r.coworkers c JOIN r.otDates d " +
            "WHERE (r.requester = :user OR c = :user) " +
            "AND r.status = com.sam.vehicle_management_system.models.OTStatus.APPROVED " +
            "AND d.workDate BETWEEN :startDate AND :endDate")
    @EntityGraph(value = "ot-request-details-graph")
    List<OTRequest> findApprovedRequestsForUserInDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @EntityGraph(value = "ot-request-details-graph")
    Optional<OTRequest> findById(Long id);

    List<OTRequest> findByStatus(OTStatus status);
}