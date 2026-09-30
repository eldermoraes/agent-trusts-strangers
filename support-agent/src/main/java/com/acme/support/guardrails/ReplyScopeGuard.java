package com.acme.support.guardrails;

import java.util.regex.Pattern;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.acme.support.CurrentTicket;
import com.acme.support.DemoStage;
import com.acme.support.ToolAudit;
import com.acme.support.clients.TicketApi;
import com.acme.support.customers.CustomerStore;

import io.quarkiverse.langchain4j.guardrails.ToolInputGuardrail;
import io.quarkiverse.langchain4j.guardrails.ToolInputGuardrailRequest;
import io.quarkiverse.langchain4j.guardrails.ToolInputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Tool-call guardrail: a reply on a ticket may only carry the ticket owner's own data.
 * The stranger owns the ticket, so "reply to the owner" is not a check;
 * "only the owner's data" is.
 */
@ApplicationScoped
public class ReplyScopeGuard implements ToolInputGuardrail {

    /** Anything that looks like a credential. Heuristic, and said so on stage. */
    static final Pattern SECRET = Pattern.compile("\\b(sk|key|token)[-_][A-Za-z0-9_-]{8,}\\b", Pattern.CASE_INSENSITIVE);

    @Inject
    DemoStage stage;

    @Inject
    CustomerStore customers;

    @RestClient
    TicketApi tickets;

    @Inject
    CurrentTicket currentTicket;

    @Inject
    ToolAudit audit;

    @Override
    public ToolInputGuardrailResult validate(ToolInputGuardrailRequest request) {
        if (!stage.enabled(DemoStage.TOOL_GUARDRAIL)) {
            return ToolInputGuardrailResult.success();
        }
        var args = request.argumentsAsJson();
        int ticketId = args.getInteger("ticketId", -1);
        if (stage.enabled(DemoStage.SCOPED_TOOLS)) {
            // same id the tool will write to, not the one the model asked for
            ticketId = currentTicket.id().orElse(ticketId);
        }
        String body = args.getString("body", "");
        String owner = tickets.get(ticketId).customerEmail();
        var result = check(owner, body);
        if (result.isSuccess()) {
            return result;
        }
        // The reason goes to the log, not to the model: a detailed refusal is a hint to retry.
        audit.record("replyToTicket", "#" + ticketId, "Blocked by guardrail: " + result.errorMessage());
        return ToolInputGuardrailResult.fatal("Request stopped by a guardrail.", null);
    }

    ToolInputGuardrailResult check(String ownerEmail, String body) {
        if (SECRET.matcher(body).find()) {
            return ToolInputGuardrailResult.failure("Reply blocked: it contains something that looks like a credential.");
        }
        for (var c : customers.all()) {
            if (c.email().equalsIgnoreCase(ownerEmail)) {
                continue;
            }
            for (var value : c.sensitiveValues()) {
                if (!value.isBlank() && body.contains(value)) {
                    return ToolInputGuardrailResult.failure(
                            "Reply blocked: it contains data of another customer (" + c.name() + ").");
                }
            }
        }
        return ToolInputGuardrailResult.success();
    }
}
