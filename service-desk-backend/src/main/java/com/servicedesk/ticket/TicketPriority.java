package com.servicedesk.ticket;

public enum TicketPriority {
    LOW(72), MEDIUM(24), HIGH(8), URGENT(4);

    private final int slaHours;

    TicketPriority(int slaHours) {
        this.slaHours = slaHours;
    }

    /** Hours the team has to resolve a ticket of this priority. */
    public int getSlaHours() {
        return slaHours;
    }

    public String label() {
        return name().toLowerCase();
    }
}
