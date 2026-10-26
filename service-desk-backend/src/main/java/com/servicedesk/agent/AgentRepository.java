package com.servicedesk.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findByNameIgnoreCase(String name);
    List<Agent> findAllByOrderByNameAsc();
}
