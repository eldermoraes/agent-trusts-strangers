package com.acme.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acme.support.guardrails.SpotlightingToolProvider;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;

/** R5 on its own: the server decides the ticket; the MCP tools only see that one. */
@QuarkusTest
@TestProfile(R5OnlyProfile.class)
class ScopedToolsTest {

    @Inject
    DemoStage stage;

    @Inject
    CurrentTicket currentTicket;

    @Inject
    SpotlightingToolProvider provider;

    @Test
    void onlyTheIsolatedDefenseIsOn() {
        assertTrue(stage.enabled(DemoStage.R5_SCOPED_TOOLS));
        assertFalse(stage.enabled(DemoStage.R2_LEAST_PRIVILEGE));
        assertFalse(stage.enabled(DemoStage.R4_OUTPUT_GUARDRAIL));
        assertEquals("only: tools scoped to the request", stage.describe());
    }

    @Test
    @ActivateRequestContext
    void mcpToolsOnlySeeTheRequestedTicket() {
        currentTicket.set(2);
        assertNull(provider.outOfScope(call("read_ticket", "{\"id\":2}")));
        assertTrue(provider.outOfScope(call("read_ticket", "{\"id\":1}")).startsWith("Not allowed"));
        assertEquals("This request is about ticket #2.", provider.outOfScope(call("list_open_tickets", "{}")));
    }

    @Test
    @ActivateRequestContext
    void noTicketInTheRequestMeansNoAccess() {
        currentTicket.set(null);
        assertTrue(provider.outOfScope(call("read_ticket", "{\"id\":1}")).startsWith("Not allowed"));
    }

    private static ToolExecutionRequest call(String name, String args) {
        return ToolExecutionRequest.builder().id("t").name(name).arguments(args).build();
    }
}
