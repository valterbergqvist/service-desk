package com.servicedesk.config;

import com.servicedesk.agent.AgentService;
import com.servicedesk.ticket.*;
import com.servicedesk.ticket.TicketDtos.CommentRequest;
import com.servicedesk.ticket.TicketDtos.CreateTicketRequest;
import com.servicedesk.ticket.TicketDtos.UpdateTicketRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Adds sample agents and tickets when app.seed-demo=true. */
@Component
@ConditionalOnProperty(name = "app.seed-demo", havingValue = "true")
public class DemoDataLoader implements CommandLineRunner {

    private final TicketService tickets;
    private final AgentService agents;

    public DemoDataLoader(TicketService tickets, AgentService agents) {
        this.tickets = tickets;
        this.agents = agents;
    }

    @Override
    public void run(String... args) {
        agents.create("Alex");
        agents.create("Sam");

        var vpn = tickets.create(new CreateTicketRequest(
                "Cannot connect to the VPN from home",
                "The VPN client says 'authentication failed' since this morning. My password still works for email.",
                "Maria Lindqvist", "maria.lindqvist@example.com", TicketPriority.HIGH, TicketCategory.NETWORK));
        tickets.update(vpn.id(), new UpdateTicketRequest(TicketStatus.IN_PROGRESS, null, "Alex"));
        tickets.addComment(vpn.id(), new CommentRequest("Alex", "Thanks Maria. Can you send me a screenshot of the error?"));

        tickets.create(new CreateTicketRequest(
                "Laptop battery drains in two hours",
                "My work laptop is about three years old and the battery no longer lasts a morning.",
                "Johan Berg", "johan.berg@example.com", TicketPriority.MEDIUM, TicketCategory.HARDWARE));

        tickets.create(new CreateTicketRequest(
                "Need access to the shared finance folder",
                "I joined the finance team this week and cannot open the shared drive.",
                "Priya Nair", "priya.nair@example.com", TicketPriority.LOW, TicketCategory.ACCESS));

        var printer = tickets.create(new CreateTicketRequest(
                "Printer on floor 3 is offline",
                "Nobody on floor 3 can print. The display shows a paper jam but there is none.",
                "Erik Svensson", "erik.svensson@example.com", TicketPriority.URGENT, TicketCategory.HARDWARE));
        tickets.update(printer.id(), new UpdateTicketRequest(TicketStatus.RESOLVED, null, "Sam"));
        tickets.addComment(printer.id(), new CommentRequest("Sam", "Reset the print spooler and replaced the paper sensor. Working again."));
    }
}
