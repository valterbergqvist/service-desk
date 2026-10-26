package com.servicedesk.ticket;

public enum TicketStatus {
    OPEN, IN_PROGRESS, RESOLVED, CLOSED;

    /** A ticket still needs work while it is open or in progress. */
    public boolean isActive() {
        return this == OPEN || this == IN_PROGRESS;
    }

    public String label() {
        return name().toLowerCase().replace('_', ' ');
    }
}
