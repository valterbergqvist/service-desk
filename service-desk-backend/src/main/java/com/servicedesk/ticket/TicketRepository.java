package com.servicedesk.ticket;

import com.servicedesk.agent.Agent;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    @Override
    @EntityGraph(attributePaths = {"comments", "assignee"})
    List<Ticket> findAll(Specification<Ticket> spec, Sort sort);

    @EntityGraph(attributePaths = {"comments", "assignee"})
    Optional<Ticket> findWithDetailsById(Long id);

    long countByStatus(TicketStatus status);

    long countByStatusInAndDueAtBefore(Collection<TicketStatus> statuses, LocalDateTime time);

    long countByAssigneeAndStatusIn(Agent assignee, Collection<TicketStatus> statuses);
}
