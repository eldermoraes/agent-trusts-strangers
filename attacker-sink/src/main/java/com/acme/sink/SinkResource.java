package com.acme.sink;

import java.util.Base64;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

/**
 * The stranger's server. It answers every request with 200 and writes down what
 * arrived. On stage this log sits next to the support panel: the moment a line
 * shows up here, data has left the building.
 */
@Path("/")
public class SinkResource {

    // 1x1 transparent GIF, so browsers happily "load" any image we serve.
    private static final byte[] PIXEL = Base64.getDecoder()
            .decode("R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7");

    @Inject
    RequestLog log;

    @GET
    @Path("/log")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RequestLog.Entry> entries() {
        return log.entries();
    }

    @DELETE
    @Path("/log")
    public Response clear() {
        log.clear();
        return Response.noContent().build();
    }

    /** Catch-all for GET: image beacons, links, anything. */
    @GET
    @Path("/{path:.+}")
    public Response get(String path, @Context UriInfo uri) {
        log.record("GET", "/" + path, uri.getRequestUri().getRawQuery(), null);
        return Response.ok(PIXEL, "image/gif").build();
    }

    /** Catch-all for POST: "emails", webhooks, whatever the agent was told to send. */
    @POST
    @Path("/{path:.+}")
    public Response post(String path, @Context UriInfo uri, String body) {
        log.record("POST", "/" + path, uri.getRequestUri().getRawQuery(), body);
        return Response.ok().build();
    }
}
