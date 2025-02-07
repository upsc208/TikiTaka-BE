//package com.trillion.tikitaka;
//
//import com.trillion.tikitaka.category.domain.Category;
//import com.trillion.tikitaka.category.infrastructure.CategoryRepository;
//import com.trillion.tikitaka.registration.domain.Registration;
//import com.trillion.tikitaka.registration.infrastructure.RegistrationRepository;
//import com.trillion.tikitaka.ticket.domain.Ticket;
//import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
//import com.trillion.tikitaka.tickettype.domain.TicketType;
//import com.trillion.tikitaka.tickettype.infrastructure.TicketTypeRepository;
//import com.trillion.tikitaka.user.domain.Role;
//import com.trillion.tikitaka.user.domain.User;
//import com.trillion.tikitaka.user.infrastructure.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.stream.Collectors;
//
//@Component
//@RequiredArgsConstructor
//public class SettingInitializer implements CommandLineRunner {
//
//    private final UserRepository userRepository;
//    private final RegistrationRepository registrationRepository;
//    private final CategoryRepository categoryRepository;
//    private final TicketTypeRepository ticketTypeRepository;
//    private final TicketRepository ticketRepository;
//
//    @Override
//    public void run(String... args) {
//        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        String encodedPassword = passwordEncoder.encode("Password1234!");
//
//        // ----------------- Default User -----------------
//        User adminUse = User.builder()
//                .username("admin.tk")
//                .password(encodedPassword)
//                .email("admin@dktechin.co.kr")
//                .role(Role.ADMIN)
//                .build();
//        userRepository.save(adminUse);
//
//        Registration regAdmin = Registration.builder()
//                .username(adminUse.getUsername())
//                .email(adminUse.getEmail())
//                .build();
//        regAdmin.approve("관리자 계정");
//        registrationRepository.save(regAdmin);
//
//        User managerUse = User.builder()
//                .username("manager.tk")
//                .password(encodedPassword)
//                .email("manager@dktechin.co.kr")
//                .role(Role.MANAGER)
//                .build();
//        userRepository.save(managerUse);
//
//        Registration regManager = Registration.builder()
//                .username(managerUse.getUsername())
//                .email(managerUse.getEmail())
//                .build();
//        regManager.approve("매니저 계정");
//        registrationRepository.save(regManager);
//
//        User userUse = User.builder()
//                .username("user.tk")
//                .password(encodedPassword)
//                .email("user@dktechin.co.kr")
//                .role(Role.USER)
//                .build();
//        userRepository.save(userUse);
//
//        Registration regUser = Registration.builder()
//                .username(userUse.getUsername())
//                .email(userUse.getEmail())
//                .build();
//        regUser.approve("사용자 계정");
//        registrationRepository.save(regUser);
//
//        for (int i = 1; i <= 10; i++) {
//            String username = String.format("testmgr%02d.tk", i);
//            String email = String.format("testmgr%02d@dktechin.co.kr", i);
//            User testManager = User.builder()
//                    .username(username)
//                    .password(encodedPassword)
//                    .email(email)
//                    .role(Role.MANAGER)
//                    .build();
//            userRepository.save(testManager);
//
//            Registration regTestManager = Registration.builder()
//                    .username(testManager.getUsername())
//                    .email(testManager.getEmail())
//                    .build();
//            regTestManager.approve("테스트 매니저 계정");
//            registrationRepository.save(regTestManager);
//        }
//
//        for (int i = 1; i <= 30; i++) {
//            String username = String.format("testusr%02d.tk", i);
//            String email = String.format("testusr%02d@dktechin.co.kr", i);
//            User testUser = User.builder()
//                    .username(username)
//                    .password(encodedPassword)
//                    .email(email)
//                    .role(Role.USER)
//                    .build();
//            userRepository.save(testUser);
//
//            Registration regTestUser = Registration.builder()
//                    .username(testUser.getUsername())
//                    .email(testUser.getEmail())
//                    .build();
//            regTestUser.approve("테스트 사용자 계정");
//            registrationRepository.save(regTestUser);
//        }
//
//        for (int i = 1; i <= 10; i++) {
//            String username = String.format("testpd%02d.tk", i);
//            String email = String.format("testpd%02d@dktechin.co.kr", i);
//
//            Registration regTestUser = Registration.builder()
//                    .username(username)
//                    .email(email)
//                    .build();
//            registrationRepository.save(regTestUser);
//        }
//
//        for (int i = 1; i <= 10; i++) {
//            String username = String.format("testrj%02d.tk", i);
//            String email = String.format("testrj%02d@dktechin.co.kr", i);
//
//            Registration regTestUser = Registration.builder()
//                    .username(username)
//                    .email(email)
//                    .build();
//            regTestUser.reject("테스트 사용자 계정 승인 거절");
//            registrationRepository.save(regTestUser);
//        }
//
//        // ----------------- Category -----------------
//        Category category1 = Category.builder()
//                .name("COMPUTE SERVICE")
//                .build();
//
//        Category category2 = Category.builder()
//                .name("NETWORKING SERVICE")
//                .build();
//
//        Category category3 = Category.builder()
//                .name("CONTAINER PACK")
//                .build();
//
//        Category category4 = Category.builder()
//                .name("STORAGE SERVICE")
//                .build();
//
//        Category category5 = Category.builder()
//                .name("DATA STORE")
//                .build();
//
//        categoryRepository.save(category1);
//        categoryRepository.save(category2);
//        categoryRepository.save(category3);
//        categoryRepository.save(category4);
//        categoryRepository.save(category5);
//
//        Category subCategory1 = Category.builder()
//                .name("VM")
//                .parent(category1)
//                .build();
//
//        Category subCategory2 = Category.builder()
//                .name("GPU")
//                .parent(category1)
//                .build();
//
//        Category subCategory3 = Category.builder()
//                .name("BARE METAL SERVER")
//                .parent(category1)
//                .build();
//
//        Category subCategory4 = Category.builder()
//                .name("vpc")
//                .parent(category2)
//                .build();
//
//        Category subCategory5 = Category.builder()
//                .name("LOAD BALANCING")
//                .parent(category2)
//                .build();
//
//        Category subCategory6 = Category.builder()
//                .name("CDN")
//                .parent(category2)
//                .build();
//
//        Category subCategory7 = Category.builder()
//                .name("DNS")
//                .parent(category2)
//                .build();
//
//        Category subCategory8 = Category.builder()
//                .name("TRANSIT GATEWAY")
//                .parent(category2)
//                .build();
//
//        Category subCategory9 = Category.builder()
//                .name("KUBERNETES ENGINE")
//                .parent(category3)
//                .build();
//
//        Category subCategory10 = Category.builder()
//                .name("CONTAINER REGISTRY")
//                .parent(category3)
//                .build();
//
//        Category subCategory11 = Category.builder()
//                .name("OBJECT STORAGE")
//                .parent(category4)
//                .build();
//
//        Category subCategory12 = Category.builder()
//                .name("FILE STORAGE")
//                .parent(category4)
//                .build();
//
//        Category subCategory13 = Category.builder()
//                .name("MYSQL")
//                .parent(category5)
//                .build();
//
//        Category subCategory14 = Category.builder()
//                .name("MEMSTORE")
//                .parent(category5)
//                .build();
//
//        Category subCategory15 = Category.builder()
//                .name("POSTGRESQL")
//                .parent(category5)
//                .build();
//
//        categoryRepository.save(subCategory1);
//        categoryRepository.save(subCategory2);
//        categoryRepository.save(subCategory3);
//        categoryRepository.save(subCategory4);
//        categoryRepository.save(subCategory5);
//        categoryRepository.save(subCategory6);
//        categoryRepository.save(subCategory7);
//        categoryRepository.save(subCategory8);
//        categoryRepository.save(subCategory9);
//        categoryRepository.save(subCategory10);
//        categoryRepository.save(subCategory11);
//        categoryRepository.save(subCategory12);
//        categoryRepository.save(subCategory13);
//        categoryRepository.save(subCategory14);
//        categoryRepository.save(subCategory15);
//
//        // ----------------- TicketType -----------------
//        TicketType ticketType1 = TicketType.builder()
//                .name("CREATE")
//                .build();
//
//        TicketType ticketType2 = TicketType.builder()
//                .name("UPDATE")
//                .build();
//
//        TicketType ticketType3 = TicketType.builder()
//                .name("DELETE")
//                .build();
//
//        TicketType ticketType4 = TicketType.builder()
//                .name("ETC")
//                .build();
//
//        ticketTypeRepository.save(ticketType1);
//        ticketTypeRepository.save(ticketType2);
//        ticketTypeRepository.save(ticketType3);
//        ticketTypeRepository.save(ticketType4);
//
//        // ----------------- Ticket -----------------
//        // 티켓 타입 목록
//        List<TicketType> ticketTypes = List.of(ticketType1, ticketType2, ticketType3, ticketType4);
//
//        // 1차 카테고리 목록 (이미 생성된 카테고리)
//        List<Category> primaryCategories = List.of(category1, category2, category3, category4, category5);
//
//        // 각 1차 카테고리별 2차 카테고리 매핑 (2차 카테고리가 없는 경우도 고려)
//        Map<Category, List<Category>> subCategoriesMap = new HashMap<>();
//        subCategoriesMap.put(category1, List.of(subCategory1, subCategory2, subCategory3));  // COMPUTE SERVICE
//        subCategoriesMap.put(category2, List.of(subCategory4, subCategory5, subCategory6, subCategory7, subCategory8));  // NETWORKING SERVICE
//        subCategoriesMap.put(category3, List.of(subCategory9, subCategory10));  // CONTAINER PACK
//        subCategoriesMap.put(category4, List.of(subCategory11, subCategory12));  // STORAGE SERVICE
//        subCategoriesMap.put(category5, List.of(subCategory13, subCategory14, subCategory15));  // DATA STORE
//
//        // 전체 사용자 목록 (요청자와 담당자 선택에 사용)
//        List<User> allUsers = userRepository.findAll();
//
//        // 매니저 역할을 가진 사용자만 필터링 (담당자 지정용)
//        List<User> managers = allUsers.stream()
//                .filter(userEntity -> userEntity.getRole() == Role.MANAGER)
//                .collect(Collectors.toList());
//
//        // 요청자는 전체 사용자 중에서 랜덤 선택 (반드시 지정됨)
//        List<User> requesters = allUsers;
//
//        for (int i = 1; i <= 200; i++) {
//            // 제목 및 설명 설정
//            String title = "티켓 제목 " + i;
//            String description = "티켓 " + i + "에 대한 상세 설명입니다.";
//
//            // 우선순위 랜덤 선택 (HIGH, MIDDLE, LOW)
//            Ticket.Priority[] priorities = Ticket.Priority.values();
//            Ticket.Priority priority = priorities[ThreadLocalRandom.current().nextInt(priorities.length)];
//
//            // 상태 랜덤 선택 (PENDING, IN_PROGRESS, DONE, REVIEW, REJECTED)
//            Ticket.Status[] statuses = Ticket.Status.values();
//            Ticket.Status status = statuses[ThreadLocalRandom.current().nextInt(statuses.length)];
//
//            // 티켓 타입 랜덤 선택
//            TicketType ticketType = ticketTypes.get(ThreadLocalRandom.current().nextInt(ticketTypes.size()));
//
//            // 1차 카테고리 랜덤 선택 (항상 지정)
//            Category primaryCategory = primaryCategories.get(ThreadLocalRandom.current().nextInt(primaryCategories.size()));
//
//            // 해당 1차 카테고리에 속한 2차 카테고리 중에서, 50% 확률로 선택하거나 null 처리
//            List<Category> subCats = subCategoriesMap.get(primaryCategory);
//            Category subCategory = null;
//            if (subCats != null && !subCats.isEmpty() && ThreadLocalRandom.current().nextBoolean()) {
//                subCategory = subCats.get(ThreadLocalRandom.current().nextInt(subCats.size()));
//            }
//
//            // 마감 기한은 현재 시간 기준 1일부터 30일 후 사이의 랜덤 일수
//            LocalDateTime deadline = LocalDateTime.now().plusDays(ThreadLocalRandom.current().nextLong(1, 31));
//
//            // 요청자 랜덤 선택 (항상 지정)
//            User requester = requesters.get(ThreadLocalRandom.current().nextInt(requesters.size()));
//
//            // 담당자는 50% 확률로 null 처리하거나, 매니저 목록에서 랜덤 선택
//            User manager = null;
//            if (!managers.isEmpty() && ThreadLocalRandom.current().nextBoolean()) {
//                manager = managers.get(ThreadLocalRandom.current().nextInt(managers.size()));
//            }
//
//            // urgent 값은 랜덤 boolean, progress는 0부터 100 사이의 랜덤 값
//            boolean urgent = ThreadLocalRandom.current().nextBoolean();
//            double progress = 0;
//
//            // Ticket 객체 생성 (빌더 사용)
//            Ticket ticket = Ticket.builder()
//                    .title(title)
//                    .description(description)
//                    .priority(priority)
//                    .status(status)
//                    .ticketType(ticketType)
//                    .firstCategory(primaryCategory)
//                    .secondCategory(subCategory)
//                    .deadline(deadline)
//                    .requester(requester)
//                    .manager(manager)
//                    .urgent(urgent)
//                    .progress(progress)
//                    .build();
//
//            // DB에 저장
//            ticketRepository.save(ticket);
//        }
//    }
//}
