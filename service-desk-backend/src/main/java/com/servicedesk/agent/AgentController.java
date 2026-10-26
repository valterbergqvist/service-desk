package com.servicedesk.agent;

import com.servicedesk.agent.AgentDtos.AgentResponse;
import com.servicedesk.agent.AgentDtos.CreateAgentRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentService service;

    public AgentController(AgentService service) {
        this.service = service;
    }

    @GetMapping
    public List<AgentResponse> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgentResponse create(@Valid @RequestBody CreateAgentRequest request) {
        return service.create(request.name());
    }
}
