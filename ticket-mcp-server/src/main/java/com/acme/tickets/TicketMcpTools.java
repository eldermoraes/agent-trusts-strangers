package com.acme.tickets;

import java.util.stream.Collectors;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;

/**
 * The MCP surface of the ticketing system. This server is honest: it returns
 * exactly what customers wrote. That is the whole point of the demo.
 */
public class TicketMcpTools {

    @Inject
    TicketStore store;

    @Tool(name = "list_open_tickets", description = "List the support tickets that still need an answer.")
    public String listOpenTickets() {
        var open = store.open();
        if (open.isEmpty()) {
            return "No open tickets.";
        }
        return open.stream()
                .map(t -> "#" + t.id() + " [" + t.customerEmail() + "] " + t.subject())
                .collect(Collectors.joining("\n"));
    }

    @Tool(name = "read_ticket", description = "Read the full content of a support ticket by id.")
    public String readTicket(@ToolArg(description = "The ticket id") int id) {
        return store.find(id).map(Ticket::asText).orElse("Ticket #" + id + " not found.");
    }
}
