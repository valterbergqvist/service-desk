package com.servicedesk.agent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AgentDtos {

    private AgentDtos() {
    }

    public record CreateAgentRequest(@NotBlank @Size(max = 100) String name) {
    }

    public record AgentResponse(Long id, String name, long openTickets) {
    }
}
