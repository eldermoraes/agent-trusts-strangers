package com.acme.support.tools;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.acme.support.CurrentTicket;
import com.acme.support.DemoStage;
import com.acme.support.clients.TicketApi;
import com.acme.support.customers.CustomerStore;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** The one tool support genuinely needs: who is this customer? */
@ApplicationScoped
public class CustomerTools {

    @Inject
    CustomerStore customers;

    @Inject
    DemoStage stage;

    @Inject
    CurrentTicket currentTicket;

    @RestClient
    TicketApi tickets;

    @Tool("Look up a customer's profile (name, phone, address, active voucher) by e-mail address.")
    public String lookupCustomer(@P("The customer's e-mail address") String email) {
        if (stage.atLeast(DemoStage.R5_SCOPED_TOOLS)) {
            // R5: this request is about one ticket; only its owner is in scope
            var owner = currentTicket.id().map(id -> tickets.get(id).customerEmail());
            if (owner.isEmpty() || !owner.get().equalsIgnoreCase(email == null ? "" : email.trim())) {
                return "Not allowed: this request is about ticket #" + currentTicket.id().map(String::valueOf).orElse("?")
                        + "; only that ticket's customer can be looked up.";
            }
        }
        return customers.find(email).map(CustomerStore.Customer::asText)
                .orElse("No customer found for " + email);
    }
}
