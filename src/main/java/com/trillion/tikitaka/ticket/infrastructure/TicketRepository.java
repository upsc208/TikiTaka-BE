package com.trillion.tikitaka.ticket.infrastructure;

import com.trillion.tikitaka.ticket.domain.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, CustomTicketRepository {

    Page<Ticket> findByManagerId(String managerId, Pageable pageable);//TODO:managerid = Long이므로 username을 통해 managerid를 찾아서 반환하도록하기
    Page<Ticket> findByRequesterId(String requesterId, Pageable pageable);//TODO:requseterid = Long이므로 username을 통해 managerid를 찾아서 반환하도록하기
    Page<Ticket> findAll(Pageable pageable);
}

