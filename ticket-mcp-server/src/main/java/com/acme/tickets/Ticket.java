package com.acme.tickets;

import java.util.List;

/** A support ticket as stored by the (legitimate) ticketing system. */
public record Ticket(int id, String customerEmail, String subject, String body, Status status, List<String> replies) {

    public enum Status { OPEN, ANSWERED }

    public Ticket withReply(String reply) {
        var all = new java.util.ArrayList<>(replies);
        all.add(reply);
        return new Ticket(id, customerEmail, subject, body, Status.ANSWERED, List.copyOf(all));
    }

    /** Plain-text rendering, exactly what the MCP tool hands to the agent. */
    public String asText() {
        var sb = new StringBuilder()
                .append("Ticket #").append(id).append('\n')
                .append("From: ").append(customerEmail).append('\n')
                .append("Subject: ").append(subject).append('\n')
                .append("Status: ").append(status).append("\n\n")
                .append(body).append('\n');
        if (!replies.isEmpty()) {
            sb.append("\nReplies:\n");
            replies.forEach(r -> sb.append("- ").append(r).append('\n'));
        }
        return sb.toString();
    }
}
