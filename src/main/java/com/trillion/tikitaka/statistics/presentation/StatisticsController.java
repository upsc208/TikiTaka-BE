package com.trillion.tikitaka.statistics.presentation;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.global.response.ApiResponse;
import com.trillion.tikitaka.statistics.application.StatisticsService;
import com.trillion.tikitaka.tickettype.domain.TicketType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

   @GetMapping("/monCreate")
   @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
   public ApiResponse<Integer> getAllCreatedMonthlyTicket(@RequestParam int year, @RequestParam int month) {
      int monCreate = statisticsService.getAllCreatedMonthlyTicket(year, month);
      return new ApiResponse<>(monCreate);
   }
   @GetMapping("/monUrgent")
   @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
   public ApiResponse<Integer> getAllUrgentMonthlyTicket(@RequestParam int year, @RequestParam int month) {
      int monCreate = statisticsService.getAllUrgentMonthlyTicket(year, month);
      return new ApiResponse<>(monCreate);
   }
   @GetMapping("/monComplete")
   @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
   public ApiResponse<Integer> getAllCompletedMonthlyTicket(@RequestParam int year, @RequestParam int month) {
      int monCreate = statisticsService.getAllCompletedMonthlyTicket(year, month);
      return new ApiResponse<>(monCreate);
   }
   @GetMapping("/monUser")
   @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
   public ApiResponse<Integer> getAllMonthlyUserTicket(@RequestParam int year, @RequestParam int month,@AuthenticationPrincipal CustomUserDetails userDetails) {
      int monCreate = statisticsService.getAllMonthlyUserTicket(year, month,userDetails);
      return new ApiResponse<>(monCreate);
   }
   @GetMapping("/monType")
   @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
   public ApiResponse<Integer> getAllMonthlyTypeTicket(@RequestParam int year, @RequestParam int month, TicketType type) {
      int monCreate = statisticsService.getAllMonthlyTypeTicket(year, month,type);
      return new ApiResponse<>(monCreate);
   }


}
