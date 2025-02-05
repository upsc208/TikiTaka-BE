package com.trillion.tikitaka.history.infrastructure;

import com.trillion.tikitaka.history.dto.response.HistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomHistoryRepository {
    Page<HistoryResponse> getHistory(Pageable pageable, Long updatedById, Long ticketId, String updateType);
}
