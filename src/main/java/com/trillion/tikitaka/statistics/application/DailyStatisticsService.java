package com.trillion.tikitaka.statistics.application;

import com.trillion.tikitaka.statistics.dto.AllUser;
import com.trillion.tikitaka.statistics.dto.response.DailyManagerStatisticsResponse;
import com.trillion.tikitaka.statistics.dto.response.DailyStatisticsResponse;
import com.trillion.tikitaka.statistics.dto.response.DailyTypeStatisticsResponse;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import com.trillion.tikitaka.user.dto.response.UserListResponse;
import com.trillion.tikitaka.user.dto.response.UserResponse;
import com.trillion.tikitaka.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalTime;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyStatisticsService {

    private final TicketRepository ticketRepository;
    private final UserService userService; // ✅ UserService 추가하여 유저 정보 가져옴
    private final TicketTypeRepository ticketTypeRepository;

    /**
     * 금일 전체 티켓 생성, 진행중, 완료된 티켓 수 통계
     */
    public DailyStatisticsResponse getDailySummary() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);

        int createdTickets = ticketRepository.countCreatedToday(startOfToday, endOfToday);
        int inProgressTickets = ticketRepository.countInProgressToday(
                startOfToday, endOfToday, Ticket.Status.IN_PROGRESS, Ticket.Status.REVIEW
        );
        int doneTickets = ticketRepository.countDoneToday(
                startOfToday, endOfToday, Ticket.Status.DONE
        );

        return new DailyStatisticsResponse(createdTickets, inProgressTickets, doneTickets);
    }

    /**
     * ✅ 금일 담당자별 처리 중 & 완료된 티켓 수 조회 (AllUser DTO 사용)
     */
    public List<AllUser> getDailyManagerSummary() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);

        // ✅ 모든 사용자 정보 조회
        UserListResponse userListResponse = userService.findAllUsers();
        List<UserResponse> users = userListResponse.getUsers();

        List<AllUser> managerStats = new ArrayList<>();

        for (UserResponse userResponse : users) {
            if (userResponse.getRole() == Role.MANAGER) { // ✅ 담당자인 경우만 조회
                int inProgressTickets = ticketRepository.countByManagerAndStatus(
                        userResponse.getUserId(), startOfToday, endOfToday,
                        List.of(Ticket.Status.IN_PROGRESS, Ticket.Status.REVIEW)
                );

                int doneTickets = ticketRepository.countByManagerAndStatus(
                        userResponse.getUserId(), startOfToday, endOfToday,
                        List.of(Ticket.Status.DONE)
                );

                // ✅ AllUser DTO에 데이터 저장
                AllUser stats = new AllUser();
                stats.updateAllUser(
                        userResponse.getUsername(),
                        userResponse.getEmail(),
                        userResponse.getUserId(),
                        userResponse.getProfileImageUrl(),
                        doneTickets,
                        inProgressTickets
                );

                managerStats.add(stats);
            }
        }
        return managerStats;
    }

    /**
     * 📌 일간 유형별 티켓 생성 현황 (기존 서비스 파일에 추가)
     */
    public List<DailyTypeStatisticsResponse> getDailyTypeSummary() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay(); // 오늘 00:00:00
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX); // 오늘 23:59:59

        List<TicketType> ticketTypes = ticketTypeRepository.findAll(); // 모든 티켓 유형 조회

        return ticketTypes.stream()
                .map(ticketType -> {
                    int count = ticketRepository.countByCreatedAtBetweenAndTicketType(startOfDay, endOfDay, ticketType);
                    return new DailyTypeStatisticsResponse(ticketType.getId(), ticketType.getName(), count);
                })
                .collect(Collectors.toList());
    }


}
