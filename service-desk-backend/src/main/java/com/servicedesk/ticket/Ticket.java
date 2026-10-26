package com.servicedesk.ticket;

import com.servicedesk.agent.Agent;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A support ticket. State changes go through the methods below so that the
 * SLA deadline, the resolved time and the activity log always stay consistent.
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(nullable = false)
    private String requesterName;

    @Column(nullable = false)
    private String requesterEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private Agent assignee;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private LocalDateTime dueAt;

    private LocalDateTime resolvedAt;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC, id ASC")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC, id ASC")
    private List<TicketEvent> events = new ArrayList<>();

    protected Ticket() {
    }

    public Ticket(String title, String description, String requesterName, String requesterEmail,
                  TicketPriority priority, TicketCategory category) {
        this.title = title;
        this.description = description;
        this.requesterName = requesterName;
        this.requesterEmail = requesterEmail;
        this.priority = priority;
        this.category = category;
        this.dueAt = LocalDateTime.now().plusHours(priority.getSlaHours());
        addEvent(EventType.CREATED, "Ticket created with " + priority.label() + " priority");
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ---------- behaviour ----------

    public void changeStatus(TicketStatus next) {
        if (next == status) {
            return;
        }
        TicketStatus previous = status;
        status = next;
        if (next.isActive()) {
            resolvedAt = null;
        } else if (resolvedAt == null) {
            resolvedAt = LocalDateTime.now();
        }
        addEvent(EventType.STATUS_CHANGED, "Status changed from " + previous.label() + " to " + next.label());
    }

    public void changePriority(TicketPriority next) {
        if (next == priority) {
            return;
        }
        TicketPriority previous = priority;
        priority = next;
        dueAt = createdAt.plusHours(next.getSlaHours());
        addEvent(EventType.PRIORITY_CHANGED, "Priority changed from " + previous.label() + " to " + next.label());
    }

    public void assignTo(Agent agent) {
        String before = assignee == null ? null : assignee.getName();
        String after = agent == null ? null : agent.getName();
        if (Objects.equals(before, after)) {
            return;
        }
        assignee = agent;
        if (agent == null) {
            addEvent(EventType.UNASSIGNED, "Unassigned from " + before);
        } else {
            addEvent(EventType.ASSIGNED, "Assigned to " + after);
        }
    }

    public Comment addComment(String author, String message) {
        Comment comment = new Comment(this, author, message);
        comments.add(comment);
        addEvent(EventType.COMMENTED, author + " replied");
        return comment;
    }

    public boolean isOverdue() {
        return status.isActive() && dueAt.isBefore(LocalDateTime.now());
    }

    void addEvent(EventType type, String message) {
        events.add(new TicketEvent(this, type, message));
        updatedAt = LocalDateTime.now();
    }

    // ---------- getters ----------

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getRequesterName() { return requesterName; }
    public String getRequesterEmail() { return requesterEmail; }
    public TicketStatus getStatus() { return status; }
    public TicketPriority getPriority() { return priority; }
    public TicketCategory getCategory() { return category; }
    public Agent getAssignee() { return assignee; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDueAt() { return dueAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public List<Comment> getComments() { return comments; }
    public List<TicketEvent> getEvents() { return events; }
}
