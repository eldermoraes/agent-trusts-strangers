package com.acme.support.tools;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.acme.support.CurrentTicket;
import com.acme.support.DemoStage;
import com.acme.support.ToolAudit;
import com.acme.support.clients.TicketApi;
import com.acme.support.guardrails.ReplyScopeGuard;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import io.quarkiverse.langchain4j.guardrails.ToolInputGuardrails;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Answer a ticket. Note what is NOT a parameter: the recipient. The ticketing
 * system decides who reads the reply (the ticket owner), the model does not.
 */
@ApplicationScoped
public class TicketReplyTools {

    @RestClient
    TicketApi tickets;

    @Inject
    DemoStage stage;

    @Inject
    CurrentTicket currentTicket;

    @Inject
    ToolAudit audit;

    @Tool("Post a reply on a support ticket. The customer who opened the ticket will read it.")
    @ToolInputGuardrails(ReplyScopeGuard.class)
    public String replyToTicket(@P("The ticket id") int ticketId,
                                @P("The reply text") String body) {
        if (stage.enabled(DemoStage.R5_SCOPED_TOOLS)) {
            // R5: the server knows which ticket this request is about; the model's choice is ignored
            if (currentTicket.id().isEmpty()) {
                audit.record("replyToTicket", "#" + ticketId, "Not allowed: no ticket in the request");
                return "Not allowed: this request does not name a ticket.";
            }
            ticketId = currentTicket.id().get();
        }
        var t = tickets.reply(ticketId, new TicketApi.NewReply(body));
        audit.record("replyToTicket", "#" + t.id(), "posted");
        return "Reply posted on ticket #" + t.id() + " (status " + t.status() + ")";
    }
}
