package com.trillion.tikitaka.statistics.application;

import com.trillion.tikitaka.category.domain.Category;
import com.trillion.tikitaka.category.dto.response.CategoryResponse;
import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
import com.trillion.tikitaka.statistics.domain.MonthlyStatistics;
import com.trillion.tikitaka.statistics.dto.response.AllCategory;
import com.trillion.tikitaka.statistics.dto.response.AllMonth;
import com.trillion.tikitaka.statistics.dto.response.AllType;
import com.trillion.tikitaka.statistics.dto.response.AllUser;
import com.trillion.tikitaka.statistics.infrastructure.MonthlyStatisticsRepository;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.tickettype.application.TicketTypeService;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import com.trillion.tikitaka.tickettype.dto.response.TicketTypeListResponse;
import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
import com.trillion.tikitaka.user.application.UserService;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.dto.response.UserListResponse;
import com.trillion.tikitaka.user.dto.response.UserResponse;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import com.trillion.tikitaka.statistics.dto.response.DailyCompletionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final TicketTypeService ticketTypeService;
    private final UserService userService;


    public List<AllCategory> getAllCategoryTicket(int year, int month) {
        List<Category> categories = categoryRepository.findAll();
        List<AllCategory> allCategories = new ArrayList<>();

        for (Category category : categories) {
            boolean isFirstCategory = (category.getParent() == null);

            if(!isFirstCategory) {

                int totalCreated = ticketRepository.countByCreatedAtBetweenAndCategoryAndUserAndType(
                        year, month, category, null, null);


                AllCategory allCategory = new AllCategory();
                allCategory.updateAllCategory(
                        category.getParent().getName(),
                        category.getName(),
                        totalCreated
                );

                allCategories.add(allCategory);

            }else{
                int totalCreated = ticketRepository.countByCreatedAtBetweenAndCategoryAndUserAndType(
                        year, month, category, null, null);

                AllCategory allCategory = new AllCategory();
                allCategory.updateAllCategory(
                        category.getName(),
                        null,
                        totalCreated
                );

                allCategories.add(allCategory);
            }
        }

        return allCategories;
    }


    public AllMonth getAllMonthlyTicket(int year,int month){
        int totalCreated = ticketRepository.countByCreatedAtBetweenAndCategoryAndUserAndType(year, month, null, null, null);
        int urgentTickets = ticketRepository.countUrgentTicketsByCategoryAndUserAndType(year, month, null,null,null);
        int totalCompleted = ticketRepository.countByCompletedStatusAndCategoryAndUserAndType(year, month, null,null,null);
        AllMonth allMonth = new AllMonth();
        allMonth.updateAllMonth(totalCreated,urgentTickets,totalCompleted);
        return allMonth;
    }


    public List<AllType> getAllTypeTicket(int year, int month) {
        List<TicketTypeListResponse> ticketTypes = ticketTypeService.getTicketTypes();

        List<AllType> allTypes = new ArrayList<>();

        for (TicketTypeListResponse ticketTypeResponse : ticketTypes) {

            TicketType ticketType = ticketTypeRepository.findById(ticketTypeResponse.getTypeId())
                    .orElse(null);

            int totalCreated = ticketRepository.countByCreatedAtBetweenAndCategoryAndUserAndType(
                    year, month, null, null,ticketType);

            AllType allType = new AllType();
            allType.updateAllType(
                    ticketTypeResponse.getTypeId(),
                    ticketTypeResponse.getTypeName(),
                    totalCreated
            );

            allTypes.add(allType);
        }

        return allTypes;
    }


    public List<AllUser> getAllUserTicket(int year, int month) {
        List<UserResponse> users = userRepository.getAllUsers();

        List<AllUser> allUsers = new ArrayList<>();

        for (UserResponse userResponse : users) {
            if (userResponse.getRole() == Role.MANAGER) {
                int totalCreated = ticketRepository.countByCreatedAtBetweenAndCategoryAndUserAndType(
                        year, month, null, userRepository.findById(userResponse.getUserId()).orElse(null), null);

                AllUser allUser = new AllUser();
                allUser.updateAllUser(
                        userResponse.getUsername(),
                        userResponse.getEmail(),
                        userResponse.getUserId(),
                        userResponse.getProfileImageUrl(),
                        totalCreated
                );

                allUsers.add(allUser);
            }
        }

        return allUsers;
    }


    public DailyCompletionResponse getDailyCompletionStatistics(Long userId) {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime todayEnd = todayStart.plusDays(1).minusNanos(1);

        int createdTickets = ticketRepository.countByCreatedAtAndUserId(todayStart, todayEnd, userId);
        int doneTickets = ticketRepository.countByCompletedAtAndUserId(todayStart, todayEnd, userId);

        return new DailyCompletionResponse(createdTickets, doneTickets);
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

        Optional<MonthlyStatistics> existingStat = statisticsRepository.findByStatYearAndStatMonthAndCategoryIdAndUserIdAndTypeId(
                year, month, categoryId, userId, typeId);

        int totalCreated = ticketRepository.countByCreatedAtBetweenAndCategoryAndUserAndType(year, month, category, user, type);
        int totalCompleted = ticketRepository.countByCompletedStatusAndCategoryAndUserAndType(year, month, category, user, type);
        int urgentTickets = ticketRepository.countUrgentTicketsByCategoryAndUserAndType(year, month, category, user, type);
        int inProgressCount = ticketRepository.countByStatusAndCategoryAndUserAndType(year, month, category, user, type, Ticket.Status.IN_PROGRESS);

        float completionRatio = (totalCreated == 0) ? 0 : (totalCompleted * 100f / totalCreated);

        if (existingStat.isPresent()) {
            MonthlyStatistics ms = existingStat.get();
            ms.updateStatistics(totalCreated, totalCompleted, urgentTickets, inProgressCount, completionRatio);
        } else {
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
                    .averageCompletionTime(0f)
                    .completionRatio(completionRatio)
                    .build();

            statisticsRepository.save(ms);
        }
    }

}