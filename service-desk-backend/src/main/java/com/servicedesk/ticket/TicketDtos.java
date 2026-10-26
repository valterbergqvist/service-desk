package com.servicedesk.ticket;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Request and response shapes of the ticket API. */
public final class TicketDtos {

    private TicketDtos() {
    }

    public record CreateTicketRequest(
            @NotBlank @Size(max = 150) String title,
            @NotBlank @Size(max = 4000) String description,
            @NotBlank @Size(max = 100) String requesterName,
            @NotBlank @Email String requesterEmail,
            TicketPriority priority,
            TicketCategory category) {
    }

    /** Every field is optional and only the fields that are sent change. An empty assignee unassigns. */
    public record UpdateTicketRequest(
            TicketStatus status,
            TicketPriority priority,
            @Size(max = 100) String assignee) {
    }

    public record CommentRequest(
            @NotBlank @Size(max = 100) String author,
            @NotBlank @Size(max = 2000) String message) {
    }

    public record CommentResponse(Long id, String author, String message, LocalDateTime createdAt) {
    }

    public record EventResponse(Long id, EventType type, String message, LocalDateTime createdAt) {
    }

    public record TicketResponse(
            Long id,
            String reference,
            String title,
            String description,
            String requesterName,
            String requesterEmail,
            TicketStatus status,
            TicketPriority priority,
            TicketCategory category,
            String assignee,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime dueAt,
            LocalDateTime resolvedAt,
            boolean overdue,
            List<CommentResponse> comments) {
    }

    public record StatsResponse(long total, Map<TicketStatus, Long> byStatus, long overdue) {
    }
}
