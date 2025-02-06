package com.trillion.tikitaka.statistics.application;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.statistics.domain.MonthlyStatistics;
import com.trillion.tikitaka.statistics.infrastructure.MonthlyStatisticsRepository;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final MonthlyStatisticsRepository statisticsRepository;
    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;

    /*@Scheduled(cron = "0 0 0 * * *")
    public void runScheduledStatisticsUpdate() {
        int year = java.time.LocalDate.now().getYear();
        int month = java.time.LocalDate.now().getMonthValue();
        updateMonthlyStatistics(year, month);
    }*/

    public void getAllMonthlyCategoryTicket(int year,int month,Category category){ // 카테고리별 티켓 조회 , 지금 1차카테고리로만 조회가능 ex) 1차카테고리 : 1 / 2차카테고리 : 2 , 1차카테고리 : 1 / 2차카테고리 : 3 이면 1차카데고리인 티켓 2개로 판정

    }
    public int getAllCreatedMonthlyTicket(int year,int month){ // 당월 기준 티켓 (이번달 생성)
        int totalCreated = ticketRepository.countByCreatedAtBetweenAndCategoryAndUserAndType(year, month, null, null, null);
        return totalCreated;
    }
    public int getAllUrgentMonthlyTicket(int year,int month){ // 당월 기준 티켓 (긴급티켓 갯수)
        int urgentTickets = ticketRepository.countUrgentTicketsByCategoryAndUserAndType(year, month, null,null,null);
        return urgentTickets;

    }
    public int getAllCompletedMonthlyTicket(int year,int month){ // 당월 기준 티켓 (당월 완료된 티켓)
        int totalCompleted = ticketRepository.countByCompletedStatusAndCategoryAndUserAndType(year, month, null,null,null);
        return totalCompleted;
    }
    public int getAllMonthlyUserTicket(int year,int month,CustomUserDetails userDetails){ // 이번달 생성된 티켓중 담당자 본인인 티켓 전부
        int totalCreated = ticketRepository.countByCreatedAtBetweenAndCategoryAndUserAndType(year, month, null, userDetails.getUser(), null);
        return totalCreated;
    }
    public int getAllMonthlyTypeTicket(int year,int month,TicketType type){ // 티켓 유형별
        int totalType = ticketRepository.countByCreatedAtBetweenAndType(year,month,type);
        return totalType;
    }





    @Transactional
    public void updateMonthlyStatistics(int year, int month) {
        List<Category> categories = categoryRepository.findAll();
        List<User> users = userRepository.findAll();
        List<TicketType> types = ticketTypeRepository.findAll();

        saveOrUpdateStatistics(year, month, null, null, null);
        for (Category c : categories) saveOrUpdateStatistics(year, month, c, null, null);
        for (User u : users) saveOrUpdateStatistics(year, month, null, u, null);
        for (TicketType t : types) saveOrUpdateStatistics(year, month, null, null, t);
    }

    @Transactional
    public void saveOrUpdateStatistics(int year, int month, Category category, User user, TicketType type) {
        Long categoryId = (category != null) ? category.getId() : null;
        Long userId = (user != null) ? user.getId() : null;
        Long typeId = (type != null) ? type.getId() : null;

        boolean exists = statisticsRepository.existsByStatYearAndStatMonthAndCategoryIdAndUserIdAndTypeId(year, month, categoryId, userId, typeId);
        if (!exists) {
            int totalCreated = ticketRepository.countByCreatedAtBetweenAndCategoryAndUserAndType(year, month, category, user, type);
            int totalCompleted = ticketRepository.countByCompletedStatusAndCategoryAndUserAndType(year, month, category, user, type);
            int urgentTickets = ticketRepository.countUrgentTicketsByCategoryAndUserAndType(year, month, category, user, type);
            int inProgressCount = ticketRepository.countByStatusAndCategoryAndUserAndType(year, month, category, user, type, Ticket.Status.IN_PROGRESS);

            //float avgCompletionTime = ticketRepository.calculateAverageCompletionTime(year, month);
            float completionRatio = (totalCreated == 0) ? 0 : (totalCompleted * 100f / totalCreated);

            // 📌 새로 저장 (Builder 사용)
            MonthlyStatistics ms = MonthlyStatistics.builder()
                    .statYear(year)
                    .statMonth(month)
                    .category(category)
                    .user(user)
                    .type(type)
                    .totalCreated(totalCreated)
                    .totalCompleted(totalCompleted)
                    .urgentTickets(urgentTickets)
                    .inProgressCount(inProgressCount)
                    //.averageCompletionTime(avgCompletionTime)
                    .completionRatio(completionRatio)
                    .build();

            statisticsRepository.save(ms);
        }
    }
}
