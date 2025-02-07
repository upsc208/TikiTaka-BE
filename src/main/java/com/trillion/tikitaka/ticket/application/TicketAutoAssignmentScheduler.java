package com.trillion.tikitaka.ticket.application;

import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketAutoAssignmentScheduler {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional
    @Scheduled(cron = "0 0 9-18 * * *")
    public void autoAssignTickets() {
        log.info("[티켓 자동 배정 스케줄러 시작] 현재 시간: {}", LocalDateTime.now());

        LocalDateTime createdBeforeMinutes = LocalDateTime.now().minusMinutes(60);
        List<Ticket> unassignedTickets = ticketRepository.findUnassignedTickets(createdBeforeMinutes);
        log.info("[미배정 티켓 조회] 기준 시간: {} 이전, 조회된 미배정 티켓 수: {}", createdBeforeMinutes, unassignedTickets.size());

        for (Ticket ticket : unassignedTickets) {
            // 2. 현재 진행중 (대기, 처리중, 검토)인 티켓 수가 가장 적은 담당자 조회
            log.info("[티켓 배정 시작] 티켓 ID: {}", ticket.getId());
            Optional<User> candidate = selectCandidateManager();
            if (candidate.isPresent()) {
                User manager = candidate.get();
                ticket.updateManager(manager);
                ticketRepository.save(ticket);
                log.info("[티켓 배정 완료] 티켓 ID: {}에 담당자(ID: {}) 할당", ticket.getId(), manager.getId());
            } else {
                log.warn("[티켓 배정 실패] 담당자 후보 없음. 티켓 ID: {}", ticket.getId());
            }
        }

        log.info("[티켓 자동 배정 스케줄러 종료] 처리 완료");
    }

    private Optional<User> selectCandidateManager() {
        List<User> managers = userRepository.findAllByRole(Role.MANAGER);
        log.info("[담당자 조회] 총 매니저 수: {}", managers.size());
        if (managers.isEmpty()) {
            log.warn("[담당자 조회 실패] 매니저가 존재하지 않음");
            return Optional.empty();
        }

        // 담당자별 진행 중 티켓 수 조회
        Map<User, Long> managerTicketCount = new HashMap<>();
        for (User manager : managers) {
            Long count = ticketRepository.countTicketsByManagerAndStatusIn(
                    manager, Arrays.asList(Ticket.Status.PENDING, Ticket.Status.IN_PROGRESS, Ticket.Status.REVIEW)
            );
            managerTicketCount.put(manager, count);
            log.info("[매니저 진행 티켓 수] 매니저 ID: {}의 진행 중 티켓 수: {}", manager.getId(), count);
        }

        // 3. 현재 담당 중인 티켓 수가 가장 적은 담당자들을 필터링
        long minTotal = managerTicketCount.values().stream().min(Long::compareTo).orElse(0L);
        List<User> candidates = managerTicketCount.entrySet().stream()
                .filter(e -> e.getValue() == minTotal)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        log.info("[후보 필터링] 최소 진행 티켓 수: {}인 후보 수: {}", minTotal, candidates.size());

        // 4. 동일한 티켓 수를 가진 경우, 대기 중인 티켓 수가 가장 적은 담당자 선택
        if (candidates.size() > 1) {
            Map<User, Long> candidatePendingCount = new HashMap<>();
            for (User manager : candidates) {
                Long pendingCount = ticketRepository.countByManagerAndTicketStatus(manager, Ticket.Status.PENDING);
                candidatePendingCount.put(manager, pendingCount);
                log.info("[후보 대기 티켓 수] 매니저 ID: {}의 대기 티켓 수: {}", manager.getId(), pendingCount);
            }
            long minPending = candidatePendingCount.values().stream().min(Long::compareTo).orElse(0L);
            candidates = candidatePendingCount.entrySet().stream()
                    .filter(e -> e.getValue() == minPending)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            log.info("[최종 후보 선정] 최소 대기 티켓 수: {}인 후보 수: {}", minPending, candidates.size());
        }

        // 5. 최종 후보가 여러 명이면 랜덤 배정
        Random random = new Random();
        User selectedManager = candidates.get(random.nextInt(candidates.size()));
        log.info("[담당자 선택 완료] 선택된 매니저 ID: {}", selectedManager.getId());
        return Optional.of(selectedManager);
    }
}
