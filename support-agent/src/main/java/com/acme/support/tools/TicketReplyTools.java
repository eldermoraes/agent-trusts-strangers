package com.acme.support.tools;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.acme.support.clients.TicketApi;
import com.acme.support.guardrails.ReplyScopeGuard;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import io.quarkiverse.langchain4j.guardrails.ToolInputGuardrails;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Answer a ticket. Note what is NOT a parameter: the recipient. The ticketing
 * system decides who reads the reply (the ticket owner), the model does not.
 */
@ApplicationScoped
public class TicketReplyTools {

    @RestClient
    TicketApi tickets;

    @Tool("Post a reply on a support ticket. The customer who opened the ticket will read it.")
    @ToolInputGuardrails(ReplyScopeGuard.class)
    public String replyToTicket(@P("The ticket id") int ticketId,
                                @P("The reply text") String body) {
        var t = tickets.reply(ticketId, new TicketApi.NewReply(body));
        return "Reply posted on ticket #" + t.id() + " (status " + t.status() + ")";
    }
}
