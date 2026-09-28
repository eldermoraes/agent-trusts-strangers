package com.acme.support.clients;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/** REST side of the ticketing system, used by the local tools (not by the model directly). */
@RegisterRestClient(configKey = "tickets")
@Path("/tickets")
public interface TicketApi {

    record TicketView(int id, String customerEmail, String subject, String body, String status, List<String> replies) {}

    record NewReply(String body) {}

    @GET
    @Path("/{id}")
    TicketView get(int id);

    @POST
    @Path("/{id}/replies")
    TicketView reply(int id, NewReply reply);
}
