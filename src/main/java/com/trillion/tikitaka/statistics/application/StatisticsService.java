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
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // 모든 카테고리 조회
        List<Category> categories = categoryRepository.findAll();
        List<AllCategory> allCategories = new ArrayList<>();

        for (Category category : categories) {
            // 1차 카테고리인지 체크 (parent가 null이면 1차 카테고리)
            boolean isFirstCategory = (category.getParent() == null);

            if(!isFirstCategory) { //2카테고리 일때

                int totalCreated = ticketRepository.countByCreatedAtBetweenAndCategoryAndUserAndType(
                        year, month, category, null, null);


                AllCategory allCategory = new AllCategory();
                allCategory.updateAllCategory(
                        category.getParent().getName(),
                        category.getName(),
                        totalCreated
                );

                allCategories.add(allCategory);

            }else{ //1차카테고리인경우
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
        // 모든 티켓 유형 조회
        List<TicketTypeListResponse> ticketTypes = ticketTypeService.getTicketTypes();

        // 결과 리스트 생성
        List<AllType> allTypes = new ArrayList<>();

        // 각 티켓 유형별로 생성된 티켓 개수 조회
        for (TicketTypeListResponse ticketTypeResponse : ticketTypes) {
            // 티켓 유형을 DB에서 조회
            TicketType ticketType = ticketTypeRepository.findById(ticketTypeResponse.getTypeId())
                    .orElse(null);

            // 해당 유형의 티켓 개수 조회
            int totalCreated = ticketRepository.countByCreatedAtBetweenAndCategoryAndUserAndType(
                    year, month, null, null,ticketType);

            // AllType DTO 생성 및 값 설정
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
        // 모든 유저 조회
        UserListResponse userListResponse = userService.findAllUsers();
        List<UserResponse> users = userListResponse.getUsers();

        // 결과 리스트 생성
        List<AllUser> allUsers = new ArrayList<>();

        // 각 유저별로 티켓 개수 조회 (담당자인 경우만)
        for (UserResponse userResponse : users) {
            if (userResponse.getRole() == Role.MANAGER) {
                int totalCreated = ticketRepository.countByCreatedAtBetweenAndCategoryAndUserAndType(
                        year, month, null, userRepository.findById(userResponse.getUserId()).orElse(null), null);

                // AllUser DTO 생성 및 값 설정
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
            // ✅ 기존 값이 존재하면 업데이트
            MonthlyStatistics ms = existingStat.get();
            ms.updateStatistics(totalCreated, totalCompleted, urgentTickets, inProgressCount, completionRatio);
        } else {
            // ✅ 존재하지 않으면 새로 저장
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
