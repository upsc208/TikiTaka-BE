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

    // 전체 통계
    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month")
    int countByCreatedAtBetween(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month AND t.urgent = true")
    int countUrgentTickets(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month AND t.status = :status")
    int countByStatus(@Param("year") int year, @Param("month") int month, @Param("status") Ticket.Status status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.updatedAt) = :year AND MONTH(t.updatedAt) = :month AND t.status = 'DONE'")
    int countByCompletedAtBetween(@Param("year") int year, @Param("month") int month);

    // 금일 완료 티켓 조회
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.createdAt BETWEEN :startOfDay AND :endOfDay AND t.manager.id = :userId")
    int countByCreatedAtAndUserId(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay, @Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.updatedAt BETWEEN :startOfDay AND :endOfDay AND t.status = 'DONE' AND t.manager.id = :userId")
    int countByCompletedAtAndUserId(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay, @Param("userId") Long userId);
    //@Query("SELECT COALESCE(AVG(DATEDIFF(t.updatedAt, t.createdAt)), 0) FROM Ticket t WHERE YEAR(t.updatedAt) = :year AND MONTH(t.updatedAt) = :month AND t.status = 'DONE'")
    //float calculateAverageCompletionTime(@Param("year") int year, @Param("month") int month);

    // 카테고리별 통계
    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month AND t.firstCategory = :category")
    int countByCreatedAtBetweenAndCategory(@Param("year") int year, @Param("month") int month, @Param("category") Category category);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.updatedAt) = :year AND MONTH(t.updatedAt) = :month AND t.status = 'DONE' AND t.firstCategory = :category")
    int countByCompletedAtBetweenAndCategory(@Param("year") int year, @Param("month") int month, @Param("category") Category category);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month AND t.urgent = true AND t.firstCategory = :category")
    int countUrgentTicketsByCategory(@Param("year") int year, @Param("month") int month, @Param("category") Category category);

    // 담당자별 통계
    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month AND t.manager = :user")
    int countByCreatedAtBetweenAndUser(@Param("year") int year, @Param("month") int month, @Param("user") User user);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.updatedAt) = :year AND MONTH(t.updatedAt) = :month AND t.status = 'DONE' AND t.manager = :user")
    int countByCompletedAtBetweenAndUser(@Param("year") int year, @Param("month") int month, @Param("user") User user);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month AND t.urgent = true AND t.manager = :user")
    int countUrgentTicketsByUser(@Param("year") int year, @Param("month") int month, @Param("user") User user);

    // 티켓 유형별 통계
    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month AND t.ticketType = :type")
    int countByCreatedAtBetweenAndType(@Param("year") int year, @Param("month") int month, @Param("type") TicketType type);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.updatedAt) = :year AND MONTH(t.updatedAt) = :month AND t.status = 'DONE' AND t.ticketType = :type")
    int countByCompletedAtBetweenAndType(@Param("year") int year, @Param("month") int month, @Param("type") TicketType type);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month AND t.urgent = true AND t.ticketType = :type")
    int countUrgentTicketsByType(@Param("year") int year, @Param("month") int month, @Param("type") TicketType type);

    //===============================일간+주간========================================//
    
    // 금일/금주 등 특정 구간에 DONE이고 manager=?
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

    // 금일 urgent=true, createdAt in [start, end), manager=?
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

    //여기서부터 금일 처리 현황 API
    // (1) 금일 생성된 티켓 수
    @Query("""
        SELECT COUNT(t)
        FROM Ticket t
        WHERE t.createdAt >= :start
          AND t.createdAt < :end
    """)
    int countCreatedToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // (2) 금일 기준 진행 중(IN_PROGRESS, REVIEW) 상태인 티켓 수 (Enum 사용)
    @Query("""
        SELECT COUNT(t)
        FROM Ticket t
        WHERE t.updatedAt >= :start
          AND t.updatedAt < :end
          AND t.status IN (:inProgressStatuses)
    """)
    int countInProgressToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                             @Param("inProgressStatuses") Ticket.Status... inProgressStatuses);

    // (3) 금일 완료된 (DONE) 상태인 티켓 수 (Enum 사용)
    @Query("""
        SELECT COUNT(t)
        FROM Ticket t
        WHERE t.updatedAt >= :start
          AND t.updatedAt < :end
          AND t.status = :doneStatus
    """)
    int countDoneToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                       @Param("doneStatus") Ticket.Status doneStatus);

    //담당자별~
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

    //여기서부터 대기중인 티켓수 계산
    // 담당자가 본인이고 상태가 PENDING인 티켓 수 조회
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.manager.id = :managerId AND t.status = :status")
    int countByManagerAndStatus(@Param("managerId") Long managerId, @Param("status") Ticket.Status status);

    // 담당자가 상관없이 상태가 PENDING인 티켓 수 조회
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = :status")
    int countByStatus(@Param("status") Ticket.Status status);

    // 담당자가 본인 or 지정되지 않고 상태가 PENDING & URGENT인 티켓 수 조회
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = :status AND t.urgent = true")
    int countUrgentPendingTickets(@Param("status") Ticket.Status status);


    // ✅ 금일 1차 카테고리별 생성된 티켓 개수
    @Query("SELECT t.firstCategory, COUNT(t) FROM Ticket t " +
            "WHERE t.createdAt BETWEEN :startOfDay AND :endOfDay " +
            "GROUP BY t.firstCategory")
    List<Object[]> countByFirstCategoryToday(@Param("startOfDay") LocalDateTime startOfDay,
                                             @Param("endOfDay") LocalDateTime endOfDay);

    // ✅ 금일 특정 1차 카테고리 내 2차 카테고리별 생성된 티켓 개수
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

    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month " +
            "AND (:category IS NULL OR t.firstCategory = :category) " +  // firstCategory만 체크하도록 변경
            "AND (:user IS NULL OR t.manager = :user) " +
            "AND (:type IS NULL OR t.ticketType = :type)")
    int countByCreatedAtBetweenAndFirstCategoryAndUserAndType(@Param("year") int year,
                                                         @Param("month") int month,
                                                         @Param("category") Category category,
                                                         @Param("user") User user,
                                                         @Param("type") TicketType type);



    @Query("SELECT COUNT(t) FROM Ticket t WHERE YEAR(t.updatedAt) = :year AND MONTH(t.updatedAt) = :month " +
            "AND t.status = 'DONE' " +
            "AND (:category IS NULL OR t.firstCategory = :category) " +
            "AND (:user IS NULL OR t.manager = :user) " +
            "AND (:type IS NULL OR t.ticketType = :type)")
    int countByCompletedAtBetweenAndCategoryAndUserAndType(@Param("year") int year,
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