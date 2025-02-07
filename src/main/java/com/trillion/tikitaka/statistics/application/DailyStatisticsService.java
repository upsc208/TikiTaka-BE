package com.trillion.tikitaka.statistics.application;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.statistics.dto.AllUser;
import com.trillion.tikitaka.statistics.dto.response.DailyCategoryStatisticsResponse;
import com.trillion.tikitaka.statistics.dto.response.DailyManagerStatisticsResponse;
import com.trillion.tikitaka.statistics.dto.response.DailyStatisticsResponse;
import com.trillion.tikitaka.statistics.dto.response.DailyTypeStatisticsResponse;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.dto.response.UserListResponse;
import com.trillion.tikitaka.user.dto.response.UserResponse;
import com.trillion.tikitaka.user.application.UserService;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyStatisticsService {

    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final TicketTypeRepository ticketTypeRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

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
     * 금일 담당자별 처리 중 & 완료된 티켓 수 조회 (AllUser DTO 사용)
     */
    public List<AllUser> getDailyManagerSummary() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);

        List<UserResponse> users = userRepository.getAllUsers();

        List<AllUser> managerStats = new ArrayList<>();

        for (UserResponse userResponse : users) {
            if (userResponse.getRole() == Role.MANAGER) {
                int inProgressTickets = ticketRepository.countByManagerAndStatus(
                        userResponse.getUserId(), startOfToday, endOfToday,
                        List.of(Ticket.Status.IN_PROGRESS, Ticket.Status.REVIEW)
                );

                int doneTickets = ticketRepository.countByManagerAndStatus(
                        userResponse.getUserId(), startOfToday, endOfToday,
                        List.of(Ticket.Status.DONE)
                );

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
     * 일간 유형별 티켓 생성 현황
     */
    public List<DailyTypeStatisticsResponse> getDailyTypeSummary() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<TicketType> ticketTypes = ticketTypeRepository.findAll();

        return ticketTypes.stream()
                .map(ticketType -> {
                    int count = ticketRepository.countByCreatedAtBetweenAndTicketType(startOfDay, endOfDay, ticketType);
                    return new DailyTypeStatisticsResponse(ticketType.getId(), ticketType.getName(), count);
                })
                .collect(Collectors.toList());
    }

    /**
     * ✅ 금일 1차, 2차 카테고리별 생성된 티켓 수 통계
     */
    public List<DailyCategoryStatisticsResponse> getDailyCategorySummary() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<Object[]> firstCategoryStats = ticketRepository.countByFirstCategoryToday(startOfDay, endOfDay);
        List<DailyCategoryStatisticsResponse> responseList = new ArrayList<>();

        for (Object[] row : firstCategoryStats) {
            Category firstCategory = (Category) row[0];
            int firstCategoryTicketCount = ((Number) row[1]).intValue();

            List<Object[]> secondCategoryStats = ticketRepository.countBySecondCategoryToday(startOfDay, endOfDay, firstCategory);

            List<DailyCategoryStatisticsResponse.SecondCategoryInfo> secondCategories = secondCategoryStats.stream()
                    .map(subRow -> new DailyCategoryStatisticsResponse.SecondCategoryInfo(
                            ((Category) subRow[0]).getId(),
                            ((Category) subRow[0]).getName(),
                            ((Number) subRow[1]).intValue()
                    ))
                    .collect(Collectors.toList());

            responseList.add(new DailyCategoryStatisticsResponse(
                    firstCategory.getId(),
                    firstCategory.getName(),
                    secondCategories,
                    firstCategoryTicketCount
            ));
        }

        return responseList;
    }
}

