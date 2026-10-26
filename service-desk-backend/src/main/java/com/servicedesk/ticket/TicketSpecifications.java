package com.servicedesk.ticket;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/** Builds the optional filters for the ticket list as one database query. */
public final class TicketSpecifications {

    private TicketSpecifications() {
    }

    public static Specification<Ticket> withFilters(TicketStatus status, TicketPriority priority, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("requesterName")), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
