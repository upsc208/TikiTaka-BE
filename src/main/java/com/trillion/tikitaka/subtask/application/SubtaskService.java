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
                .is_Done(false)
                .build();

        return subtaskRepository.save(subtask);
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
    public void deleteSubtask(Long taskId) {
        Subtask subtask = subtaskRepository.findById(taskId)
                .orElseThrow(SubtaskNotFoundExeption::new);

        subtaskRepository.delete(subtask);
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
    public void updateSubtask(Long taskId,Boolean checkIsDone){
        Subtask subtask = subtaskRepository.findById(taskId)
                .orElseThrow(SubtaskNotFoundExeption::new);
        subtask.updateIsDone(checkIsDone);
    }



}

