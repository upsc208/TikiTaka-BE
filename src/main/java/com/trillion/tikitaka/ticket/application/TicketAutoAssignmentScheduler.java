package com.trillion.tikitaka.ticket.application;

import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.user.domain.Role;
import com.trillion.tikitaka.user.domain.User;
import com.trillion.tikitaka.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketAutoAssignmentScheduler {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional
    @Scheduled(fixedDelay = 3600000) // 1시간 마다 실행 60 * 10000 * 6
    public void autoAssignTickets() {
        // 1. 미배정 티켓 조회
        LocalDateTime createdBeforeMinutes = LocalDateTime.now().minusMinutes(60);
        List<Ticket> unassignedTickets = ticketRepository.findUnassignedTickets(createdBeforeMinutes);

        for (Ticket ticket : unassignedTickets) {
            // 2. 현재 진행중 (대기, 처리중, 검토)인 티켓 수가 가장 적은 담당자 조회
            Optional<User> candidate = selectCandidateManager();
            candidate.ifPresent(manager -> {
                ticket.updateManager(manager);
                ticketRepository.save(ticket);
            });
        }
    }

    private Optional<User> selectCandidateManager() {
        List<User> managers = userRepository.findAllByRole(Role.MANAGER);
        if (managers.isEmpty()) {
            return Optional.empty();
        }

        // 담당자별 진행 중 티켓 수 조회
        Map<User, Long> managerTicketCount = new HashMap<>();
        for (User manager : managers) {
            Long count = ticketRepository.countTicketsByManagerAndStatusIn(
                    manager, Arrays.asList(Ticket.Status.PENDING, Ticket.Status.IN_PROGRESS, Ticket.Status.REVIEW)
            );
            managerTicketCount.put(manager, count);
        }

        // 3. 현재 담당 중인 티켓 수가 가장 적은 담당자들을 필터링
        long minTotal = managerTicketCount.values().stream().min(Long::compareTo).orElse(0L);
        List<User> candidates = managerTicketCount.entrySet().stream()
                .filter(e -> e.getValue() == minTotal)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 4. 동일한 티켓 수를 가진 경우, 대기 중인 티켓 수가 가장 적은 담당자 선택
        if (candidates.size() > 1) {
            Map<User, Long> candidatePendingCount = new HashMap<>();
            for (User manager : candidates) {
                Long pendingCount = ticketRepository.countByManagerAndTicketStatus(manager, Ticket.Status.PENDING);
                candidatePendingCount.put(manager, pendingCount);
            }
            long minPending = candidatePendingCount.values().stream().min(Long::compareTo).orElse(0L);
            candidates = candidatePendingCount.entrySet().stream()
                    .filter(e -> e.getValue() == minPending)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }

        // 5. 최종 후보가 여러 명이면 랜덤 배정
        Random random = new Random();
        User selectedManager = candidates.get(random.nextInt(candidates.size()));
        return Optional.of(selectedManager);
    }
}
