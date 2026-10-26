package com.servicedesk.ticket;

import com.servicedesk.agent.Agent;
import com.servicedesk.agent.AgentRepository;
import com.servicedesk.common.NotFoundException;
import com.servicedesk.ticket.TicketDtos.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketService {

    private static final List<TicketStatus> ACTIVE = List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS);

    private final TicketRepository tickets;
    private final AgentRepository agents;

    public TicketService(TicketRepository tickets, AgentRepository agents) {
        this.tickets = tickets;
        this.agents = agents;
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> list(TicketStatus status, TicketPriority priority, String search) {
        return tickets
                .findAll(TicketSpecifications.withFilters(status, priority, search),
                        Sort.by(Sort.Direction.DESC, "createdAt", "id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public List<EventResponse> activity(Long id) {
        return find(id).getEvents().stream()
                .map(e -> new EventResponse(e.getId(), e.getType(), e.getMessage(), e.getCreatedAt()))
                .toList();
    }

    @Transactional
    public TicketResponse create(CreateTicketRequest request) {
        Ticket ticket = new Ticket(
                request.title().trim(),
                request.description().trim(),
                request.requesterName().trim(),
                request.requesterEmail().trim().toLowerCase(),
                request.priority() != null ? request.priority() : TicketPriority.MEDIUM,
                request.category() != null ? request.category() : TicketCategory.OTHER);
        return toResponse(tickets.save(ticket));
    }

    @Transactional
    public TicketResponse update(Long id, UpdateTicketRequest request) {
        Ticket ticket = find(id);
        if (request.status() != null) {
            ticket.changeStatus(request.status());
        }
        if (request.priority() != null) {
            ticket.changePriority(request.priority());
        }
        if (request.assignee() != null) {
            ticket.assignTo(findOrCreateAgent(request.assignee()));
        }
        return toResponse(tickets.saveAndFlush(ticket));
    }

    @Transactional
    public CommentResponse addComment(Long id, CommentRequest request) {
        Ticket ticket = find(id);
        Comment comment = ticket.addComment(request.author().trim(), request.message().trim());
        tickets.saveAndFlush(ticket);
        return toResponse(comment);
    }

    @Transactional
    public void delete(Long id) {
        tickets.delete(find(id));
    }

    @Transactional(readOnly = true)
    public StatsResponse stats() {
        Map<TicketStatus, Long> byStatus = new EnumMap<>(TicketStatus.class);
        long total = 0;
        for (TicketStatus status : TicketStatus.values()) {
            long count = tickets.countByStatus(status);
            byStatus.put(status, count);
            total += count;
        }
        long overdue = tickets.countByStatusInAndDueAtBefore(ACTIVE, LocalDateTime.now());
        return new StatsResponse(total, byStatus, overdue);
    }

    // ---------- helpers ----------

    private Ticket find(Long id) {
        return tickets.findWithDetailsById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));
    }

    /** An empty name unassigns the ticket; an unknown name creates a new agent. */
    private Agent findOrCreateAgent(String name) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return agents.findByNameIgnoreCase(trimmed).orElseGet(() -> agents.save(new Agent(trimmed)));
    }

    private TicketResponse toResponse(Ticket t) {
        return new TicketResponse(
                t.getId(), "SD-" + t.getId(), t.getTitle(), t.getDescription(),
                t.getRequesterName(), t.getRequesterEmail(), t.getStatus(), t.getPriority(), t.getCategory(),
                t.getAssignee() == null ? null : t.getAssignee().getName(),
                t.getCreatedAt(), t.getUpdatedAt(), t.getDueAt(), t.getResolvedAt(), t.isOverdue(),
                t.getComments().stream().map(this::toResponse).toList());
    }

    private CommentResponse toResponse(Comment c) {
        return new CommentResponse(c.getId(), c.getAuthor(), c.getMessage(), c.getCreatedAt());
    }
}
