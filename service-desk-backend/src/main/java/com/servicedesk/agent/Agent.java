package com.servicedesk.agent;

import jakarta.persistence.*;

/** A member of the support team that tickets can be assigned to. */
@Entity
@Table(name = "agents")
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    protected Agent() {
    }

    public Agent(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}
