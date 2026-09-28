package com.acme.tickets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class TicketStoreTest {

    @Inject
    TicketStore store;

    @BeforeEach
    void reset() {
        store.reset();
    }

    @Test
    void seedsOneOpenTicket() {
        assertEquals(1, store.open().size());
    }

    @Test
    void openingATicketAssignsTheNextId() {
        var t = store.open("stranger@example.net", "Hello", "Anything at all");
        assertEquals(2, t.id());
        assertEquals(2, store.open().size());
    }

    @Test
    void replyingClosesTheTicketAndKeepsTheText() {
        var t = store.reply(1, "We reshipped your order.").orElseThrow();
        assertEquals(Ticket.Status.ANSWERED, t.status());
        assertTrue(t.asText().contains("We reshipped your order."));
        assertEquals(0, store.open().size());
    }

    @Test
    void ticketTextIsVerbatim() {
        var t = store.open("x@example.com", "Subject", "Body line 1\nBody line 2");
        assertTrue(t.asText().contains("Body line 1\nBody line 2"));
    }
}
