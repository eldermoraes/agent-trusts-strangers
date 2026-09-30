package com.acme.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.acme.support.ai.SupportAgent;
import com.acme.support.clients.TicketApi;
import com.acme.support.tools.CustomerTools;
import com.acme.support.tools.EmailTools;

import io.quarkiverse.langchain4j.ToolBox;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

/** The two guarantees the talk says run in every PR, without a model. */
@QuarkusTest
@TestProfile(R5OnlyProfile.class)
class GuaranteesTest {

    @Inject
    CurrentTicket currentTicket;

    @Inject
    CustomerTools customerTools;

    @InjectMock
    @RestClient
    TicketApi tickets;

    @Test
    void leastPrivilegeLeavesTheEmailToolOut() throws Exception {
        var method = SupportAgent.class.getMethod("chatLeastPrivilege", String.class, String.class, String.class);
        List<Class<?>> tools = Arrays.asList(method.getAnnotation(ToolBox.class).value());
        assertFalse(tools.contains(EmailTools.class));
    }

    @Test
    @ActivateRequestContext
    void lookupOutsideTheRequestedTicketIsRefused() {
        Mockito.when(tickets.get(2)).thenReturn(
                new TicketApi.TicketView(2, "stranger@example.net", "Order never arrived", "…", "OPEN", List.of()));
        currentTicket.set(2);
        String answer = customerTools.lookupCustomer("victim@example.com");
        assertEquals("Not allowed: this request is about ticket #2; only that ticket's customer can be looked up.", answer);
    }

    @Test
    @ActivateRequestContext
    void theTicketOwnerIsInScope() {
        Mockito.when(tickets.get(1)).thenReturn(
                new TicketApi.TicketView(1, "victim@example.com", "Order #4471", "…", "OPEN", List.of()));
        currentTicket.set(1);
        assertTrue(customerTools.lookupCustomer("victim@example.com").startsWith("Customer: Victim"));
    }
}
