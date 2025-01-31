package com.trillion.tikitaka.ticket.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TicketCountByStatusResponse {

    private Long total;
    private Long pending;
    private Long inProgress;
    private Long reviewing;
    private Long completed;
    private Long urgent;

    @QueryProjection
    public TicketCountByStatusResponse(Long total, Long pending, Long inProgress, Long reviewing, Long completed, Long urgent) {
        this.total = total;
        this.pending = pending;
        this.inProgress = inProgress;
        this.reviewing = reviewing;
        this.completed = completed;
        this.urgent = urgent;
    }
}
