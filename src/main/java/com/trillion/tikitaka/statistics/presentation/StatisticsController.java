package com.trillion.tikitaka.statistics.presentation;
import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.statistics.application.StatisticsService;
import com.trillion.tikitaka.statistics.dto.response.AllCategory;
import com.trillion.tikitaka.statistics.dto.response.AllMonth;
import com.trillion.tikitaka.statistics.dto.response.AllType;
import com.trillion.tikitaka.statistics.dto.response.AllUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/statistic")
@RequiredArgsConstructor
public class StatisticsController {

   private final StatisticsService statisticsService;

   @PostMapping("/record") // 저장 시험
   @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
   public ApiResponse<Void> makeStatistics(@RequestParam int year,@RequestParam int month) {
      statisticsService.updateMonthlyStatistics(year, month);
      return new ApiResponse<>("성공",null);
   }

   @GetMapping("/monCategory")
   @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')") //카테고리별 전부 티켓 조회,list - 보류
   public ApiResponse<List<AllCategory>> getCategoryMonthlyTicket(@RequestParam int year, @RequestParam int month) {
      List<AllCategory> allCategory = statisticsService.getAllCategoryTicket(year,month);
      return new ApiResponse<>("성공",allCategory);
   }

   @GetMapping("/monAll")
   @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')") //당월 생성된 티켓
   public ApiResponse<AllMonth> getAllMonthlyTicketApi(@RequestParam int year, @RequestParam int month) {
      AllMonth allMonth = statisticsService.getAllMonthlyTicket(year, month);
      return new ApiResponse<>("성공", allMonth);
   }

   @GetMapping("/monUser")
   @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')") //담당자별(그냥 list not page,사용자 유저이름,아이디,이메일,프로필사진 주소)
   public ApiResponse<List<AllUser>> getUserMonthlyTicket(@RequestParam int year, @RequestParam int month) {
      List<AllUser> allUsers = statisticsService.getAllUserTicket(year, month);
      return new ApiResponse<>("성공", allUsers);
   }
   @GetMapping("/monType")
   @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')") //타입별(타입 아이디, 이름, 타입별 당월 생성된 티켓)
   public ApiResponse<List<AllType>> getAllTypeTickets(@RequestParam int year, @RequestParam int month) {
      List<AllType> allTypes = statisticsService.getAllTypeTicket(year, month);
      return new ApiResponse<>("성공", allTypes);
   }





}
