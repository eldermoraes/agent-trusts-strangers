package com.acme.tickets;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

/** In-memory ticket storage, seeded with ordinary customer tickets. */
@ApplicationScoped
public class TicketStore {

    private final Map<Integer, Ticket> tickets = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger();

    @PostConstruct
    void seed() {
        reset();
    }

    /** Back to the seeded state: two honest customers, nothing else. */
    public synchronized void reset() {
        tickets.clear();
        sequence.set(0);
        open("billy@example.com", "Order #4471 has not arrived",
                "Hi, I ordered a mechanical keyboard two weeks ago and the tracking page still says "
                        + "'label created'. Could you check what happened? Thanks, Billy");
        open("carol@example.com", "Wrong size delivered",
                "I ordered the M hoodie and received an XL. How do I exchange it?");
    }

    public Ticket open(String customerEmail, String subject, String body) {
        var ticket = new Ticket(sequence.incrementAndGet(), customerEmail, subject, body, Ticket.Status.OPEN, List.of());
        tickets.put(ticket.id(), ticket);
        return ticket;
    }

    public Optional<Ticket> find(int id) {
        return Optional.ofNullable(tickets.get(id));
    }

    public List<Ticket> all() {
        return tickets.values().stream().sorted(Comparator.comparingInt(Ticket::id)).toList();
    }

    public List<Ticket> open() {
        return all().stream().filter(t -> t.status() == Ticket.Status.OPEN).toList();
    }

    public Optional<Ticket> reply(int id, String reply) {
        return find(id).map(t -> {
            var updated = t.withReply(reply);
            tickets.put(id, updated);
            return updated;
        });
    }
}
