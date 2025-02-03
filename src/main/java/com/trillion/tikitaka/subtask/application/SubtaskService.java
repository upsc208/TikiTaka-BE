package com.trillion.tikitaka.subtask.application;

import com.trillion.tikitaka.authentication.domain.CustomUserDetails;
import com.trillion.tikitaka.subtask.domain.Subtask;
import com.trillion.tikitaka.subtask.dto.response.SubtaskResponse;
import com.trillion.tikitaka.subtask.exception.SubtaskNotFoundExeption;
import com.trillion.tikitaka.subtask.exception.UnauthrizedSubtaskAcessExeception;
import com.trillion.tikitaka.ticket.domain.Ticket;
import com.trillion.tikitaka.subtask.dto.request.SubtaskRequest;
import com.trillion.tikitaka.subtask.infrastructure.SubtaskRepository;
import com.trillion.tikitaka.ticket.exception.TicketNotFoundException;
import com.trillion.tikitaka.ticket.infrastructure.TicketRepository;
import com.trillion.tikitaka.user.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubtaskService {

    private final SubtaskRepository subtaskRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public Subtask createSubtask(SubtaskRequest request) {
        Ticket parentTicket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new TicketNotFoundException());

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
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException());

        List<Subtask> subtasks = subtaskRepository.findByParentTicket(ticket);

        return subtasks.stream()
                .map(SubtaskResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void editSubtask(Long taskId, SubtaskRequest request) {
        Subtask subtask = subtaskRepository.findById(taskId)
                .orElseThrow(UnauthrizedSubtaskAcessExeception::new);

        subtask.updateDescription(request.getDescription());
    }

    @Transactional
    public void deleteSubtask(Long taskId,Long ticketId) {
        Subtask subtask = subtaskRepository.findById(taskId)
                .orElseThrow(SubtaskNotFoundExeption::new);

        subtaskRepository.delete(subtask);
        calculateProgress(ticketId);
    }
    @Transactional
    public void deleteAllSubtask(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException());

        List<Subtask> subtasks = subtaskRepository.findAllByParentTicket(ticket);

        if (!subtasks.isEmpty()) {
            subtaskRepository.deleteAll(subtasks);
        }
    }

    @Transactional
    public void updateSubtaskIsDone(Long taskId,Boolean checkIsDone,Long ticketId){
        Subtask subtask = subtaskRepository.findById(taskId)
                .orElseThrow(SubtaskNotFoundExeption::new);
        subtask.updateIsDone(checkIsDone);
        calculateProgress(ticketId);
    }
    @Transactional
    public Double calculateProgress(Long ticketId) {
        Double subtaskCount = subtaskRepository.countAllByParentTicketId(ticketId);
        Double isDoneChecked = subtaskRepository.countAllByDoneIsTrueAndParentTicketId(ticketId);

        if (subtaskCount == 0) {
            return null;
        }

        Double progress = (isDoneChecked / subtaskCount) * 100;
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException());
        ticket.updateProgress(progress);
        
        return Math.round(progress * 10.0) / 10.0;
    }



}

