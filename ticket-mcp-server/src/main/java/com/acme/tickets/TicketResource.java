package com.acme.tickets;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Public REST face of the ticketing system: the web form anyone can use to open
 * a ticket, plus what the support agent needs to answer one.
 */
@Path("/tickets")
@Produces(MediaType.APPLICATION_JSON)
public class TicketResource {

    public record NewTicket(String customerEmail, String subject, String body) {}

    public record NewReply(String body) {}

    @Inject
    TicketStore store;

    @GET
    public List<Ticket> all() {
        return store.all();
    }

    @GET
    @Path("/{id}")
    public Ticket get(int id) {
        return store.find(id).orElseThrow(() -> new NotFoundException("Ticket #" + id + " not found"));
    }

    /** The public form. This is where the stranger walks in. */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response open(NewTicket request) {
        var ticket = store.open(request.customerEmail(), request.subject(), request.body());
        return Response.status(Response.Status.CREATED).entity(ticket).build();
    }

    @POST
    @Path("/{id}/replies")
    @Consumes(MediaType.APPLICATION_JSON)
    public Ticket reply(int id, NewReply request) {
        return store.reply(id, request.body()).orElseThrow(() -> new NotFoundException("Ticket #" + id + " not found"));
    }

    @POST
    @Path("/reset")
    public List<Ticket> reset() {
        store.reset();
        return store.all();
    }
}
