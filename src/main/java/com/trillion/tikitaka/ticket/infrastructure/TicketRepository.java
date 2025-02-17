package com.trillion.tikitaka.ticket.infrastructure;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, CustomTicketRepository {

    Page<Ticket> findByManagerId(String managerId, Pageable pageable);
    Page<Ticket> findByRequesterId(String requesterId, Pageable pageable);
    Page<Ticket> findAll(Pageable pageable);

    @Modifying
    @Query("UPDATE Ticket t SET t.deletedAt = CURRENT_TIMESTAMP WHERE t.requester.id = :userId")
    void softDeleteTicketsByRequester(@Param("userId") Long userId);

    boolean existsById(Long ticketId);

    //===============================일간+주간================================//

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.createdAt BETWEEN :startOfDay AND :endOfDay AND t.manager.id = :userId")
    int countByCreatedAtAndUserId(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay, @Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.updatedAt BETWEEN :startOfDay AND :endOfDay AND t.status = 'DONE' AND t.manager.id = :userId")
    int countByCompletedAtAndUserId(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay, @Param("userId") Long userId);


    @Query("""
        SELECT COUNT(t)
        FROM Ticket t
        WHERE t.updatedAt >= :start
          AND t.updatedAt < :end
          AND t.status = 'DONE'
          AND t.manager.id = :managerId
    """)
    int countDoneBetweenAndManager(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("managerId") Long managerId
    );


    @Query("""
        SELECT COUNT(t)
        FROM Ticket t
        WHERE t.createdAt >= :start
          AND t.createdAt < :end
          AND t.urgent = true
          AND t.manager.id = :managerId
    """)
    int countUrgentCreatedToday(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("managerId") Long managerId
    );


    @Query("""
        SELECT COUNT(t)
        FROM Ticket t
        WHERE t.createdAt >= :start
          AND t.createdAt < :end
    """)
    int countCreatedToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


    @Query("""
        SELECT COUNT(t)
        FROM Ticket t
        WHERE t.updatedAt >= :start
          AND t.updatedAt < :end
          AND t.status IN (:inProgressStatuses)
    """)
    int countInProgressToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                             @Param("inProgressStatuses") Ticket.Status... inProgressStatuses);

    @Query("""
        SELECT COUNT(t)
        FROM Ticket t
        WHERE t.updatedAt >= :start
          AND t.updatedAt < :end
          AND t.status = :doneStatus
    """)
    int countDoneToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                       @Param("doneStatus") Ticket.Status doneStatus);

    @Query("""
        SELECT COUNT(t)
        FROM Ticket t
        WHERE t.manager.id = :managerId
          AND t.updatedAt >= :start
          AND t.updatedAt < :end
          AND t.status IN (:statuses)
    """)
    int countByManagerAndStatus(@Param("managerId") Long managerId,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                @Param("statuses") List<Ticket.Status> statuses);

    int countByCreatedAtBetweenAndTicketType(LocalDateTime start, LocalDateTime end, TicketType ticketType);


    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.manager.id = :managerId AND t.status = :status")
    int countByManagerAndStatus(@Param("managerId") Long managerId, @Param("status") Ticket.Status status);


    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = :status")
    int countByStatus(@Param("status") Ticket.Status status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = :status AND t.urgent = true")
    int countUrgentPendingTickets(@Param("status") Ticket.Status status);


    @Query("SELECT t.firstCategory, COUNT(t) FROM Ticket t " +
            "WHERE t.createdAt BETWEEN :startOfDay AND :endOfDay " +
            "GROUP BY t.firstCategory")
    List<Object[]> countByFirstCategoryToday(@Param("startOfDay") LocalDateTime startOfDay,
                                             @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT t.secondCategory, COUNT(t) FROM Ticket t " +
            "WHERE t.createdAt BETWEEN :startOfDay AND :endOfDay " +
            "AND t.firstCategory = :firstCategory " +
            "GROUP BY t.secondCategory")
    List<Object[]> countBySecondCategoryToday(@Param("startOfDay") LocalDateTime startOfDay,
                                              @Param("endOfDay") LocalDateTime endOfDay,
                                              @Param("firstCategory") Category firstCategory);


    //===============================월간========================================//
    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month " +
            "AND (:category IS NULL OR " +
            "(t.secondCategory IS NOT NULL AND t.secondCategory = :category) OR " +
            "(t.secondCategory IS NULL AND t.firstCategory = :category)) " +
            "AND (:user IS NULL OR t.manager = :user) " +
            "AND (:type IS NULL OR t.ticketType = :type)")
    int countByCreatedAtBetweenAndCategoryAndUserAndType(@Param("year") int year,
                                                         @Param("month") int month,
                                                         @Param("category") Category category,
                                                         @Param("user") User user,
                                                         @Param("type") TicketType type);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.updatedAt) = :year AND MONTH(t.updatedAt) = :month " +
            "AND t.status = 'DONE' " +
            "AND (:category IS NULL OR t.firstCategory = :category) " +
            "AND (:user IS NULL OR t.manager = :user) " +
            "AND (:type IS NULL OR t.ticketType = :type)")
    int countByCompletedStatusAndCategoryAndUserAndType(@Param("year") int year,
                                                        @Param("month") int month,
                                                        @Param("category") Category category,
                                                        @Param("user") User user,
                                                        @Param("type") TicketType type);


    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month " +
            "AND t.urgent = true " +
            "AND (:category IS NULL OR t.firstCategory = :category) " +
            "AND (:user IS NULL OR t.manager = :user) " +
            "AND (:type IS NULL OR t.ticketType = :type)")
    int countUrgentTicketsByCategoryAndUserAndType(@Param("year") int year,
                                                   @Param("month") int month,
                                                   @Param("category") Category category,
                                                   @Param("user") User user,
                                                   @Param("type") TicketType type);


    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month " +
            "AND t.status = :status " +
            "AND (:category IS NULL OR t.firstCategory = :category) " +
            "AND (:user IS NULL OR t.manager = :user) " +
            "AND (:type IS NULL OR t.ticketType = :type)")
    int countByStatusAndCategoryAndUserAndType(@Param("year") int year,
                                               @Param("month") int month,
                                               @Param("category") Category category,
                                               @Param("user") User user,
                                               @Param("type") TicketType type,
                                               @Param("status") Ticket.Status status);
}