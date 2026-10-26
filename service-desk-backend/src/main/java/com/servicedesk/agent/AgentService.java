package com.servicedesk.agent;

import com.servicedesk.agent.AgentDtos.AgentResponse;
import com.servicedesk.common.ConflictException;
import com.servicedesk.ticket.TicketRepository;
import com.servicedesk.ticket.TicketStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AgentService {

    private static final List<TicketStatus> ACTIVE = List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS);

    private final AgentRepository agents;
    private final TicketRepository tickets;

    public AgentService(AgentRepository agents, TicketRepository tickets) {
        this.agents = agents;
        this.tickets = tickets;
    }

    @Transactional(readOnly = true)
    public List<AgentResponse> list() {
        return agents.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public AgentResponse create(String name) {
        String trimmed = name.trim();
        if (agents.findByNameIgnoreCase(trimmed).isPresent()) {
            throw new ConflictException("An agent with this name already exists");
        }
        return toResponse(agents.save(new Agent(trimmed)));
    }

    private AgentResponse toResponse(Agent agent) {
        return new AgentResponse(agent.getId(), agent.getName(),
                tickets.countByAssigneeAndStatusIn(agent, ACTIVE));
    }
}
