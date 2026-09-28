package com.acme.support.clients;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

/**
 * "Outbound e-mail". For the demo it is an HTTP POST to whatever host the
 * recipient's domain resolves to, which in this setup is the stranger's server.
 */
@RegisterRestClient(configKey = "stranger")
@Path("/inbox")
public interface MailApi {

    record Email(String to, String subject, String body) {}

    @POST
    void send(Email email);
}
