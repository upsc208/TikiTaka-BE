package com.trillion.tikitaka.subtask.application;

import com.trillion.tikitaka.subtask.domain.Subtask;
import com.trillion.tikitaka.subtask.dto.response.SubtaskResponse;
import com.trillion.tikitaka.subtask.exception.SubtaskNotFoundExeption;
import com.trillion.tikitaka.subtask.exception.UnauthrizedSubtaskAcessExeception;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.subtask.dto.request.SubtaskRequest;
import com.trillion.tikitaka.subtask.infrastructure.SubtaskRepository;
import com.trillion.tikitaka.ticket.exception.TicketNotFoundException;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubtaskService {

    private final SubtaskRepository subtaskRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public Subtask createSubtask(SubtaskRequest request) {
        log.info("[하위 태스트 생성]");
        Ticket parentTicket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(TicketNotFoundException::new);

        Subtask subtask = Subtask.builder()
                .description(request.getDescription())
                .parentTicket(parentTicket)
                .done(false)
                .build();
        subtaskRepository.save(subtask);
        calculateProgress(request.getTicketId());
        return subtask;
    }

    @Transactional(readOnly = true)
    public List<SubtaskResponse> getSubtasksByTicketId(Long ticketId) {
        log.info("[하위 태스크 조회]");
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);

        List<Subtask> subtasks = subtaskRepository.findByParentTicket(ticket);

        return subtasks.stream()
                .map(SubtaskResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void editSubtask(Long taskId, SubtaskRequest request) {
        log.info("[하위 태스크 수정]");
        Subtask subtask = subtaskRepository.findById(taskId)
                .orElseThrow(UnauthrizedSubtaskAcessExeception::new);

        subtask.updateDescription(request.getDescription());

    }

    @Transactional
    public void deleteSubtask(Long taskId,Long ticketId) {
        log.info("[하위 태스크 삭제]");
        Subtask subtask = subtaskRepository.findById(taskId)
                .orElseThrow(SubtaskNotFoundExeption::new);

        subtaskRepository.delete(subtask);
        calculateProgress(ticketId);
    }
    @Transactional
    public void deleteAllSubtask(Long ticketId) {
        log.info("[하위 태스크 전체 삭제]");
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);

        List<Subtask> subtasks = subtaskRepository.findAllByParentTicket(ticket);

        if (!subtasks.isEmpty()) {
            subtaskRepository.deleteAll(subtasks);
        }
    }

    @Transactional
    public void updateSubtaskIsDone(Long taskId,Boolean checkIsDone,Long ticketId){
        log.info("[하위 태스크 완료]");
        Subtask subtask = subtaskRepository.findById(taskId)
                .orElseThrow(SubtaskNotFoundExeption::new);
        subtask.updateIsDone(checkIsDone);
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);
        if(ticket.getStatus().equals(Ticket.Status.PENDING)&&subtask.getDone().equals(true)){
            ticket.updateStatus(Ticket.Status.IN_PROGRESS);
        }
        calculateProgress(ticketId);
    }
    @Transactional
    public Double calculateProgress(Long ticketId) {
        log.info("[티켓 진행률 계산]");
        Double subtaskCount = subtaskRepository.countAllByParentTicketId(ticketId);
        Double isDoneChecked = subtaskRepository.countAllByDoneIsTrueAndParentTicketId(ticketId);

        if (subtaskCount == 0) {
            log.info("[티켓 진행률 계산] 하위 태스크가 존재하지 않음");
            return null;
        }

        Double progress = (isDoneChecked / subtaskCount) * 100;
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(TicketNotFoundException::new);
        ticket.updateProgress(progress);

        log.info("[티켓 진행률 계산] 진행률: {}", progress);
        return Math.round(progress * 10.0) / 10.0;
    }



}

