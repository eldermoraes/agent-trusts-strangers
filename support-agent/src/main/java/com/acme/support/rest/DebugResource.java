package com.acme.support.rest;

import java.util.Map;

import com.acme.support.DemoStage;
import com.acme.support.guardrails.SpotlightingToolProvider;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** For the stage: what did the model actually see? */
@Path("/api/debug")
@Produces(MediaType.APPLICATION_JSON)
public class DebugResource {

    @Inject
    DemoStage stage;

    @Inject
    SpotlightingToolProvider spotlighting;

    @GET
    public Map<String, Object> debug() {
        return Map.of(
                "stage", stage.current(),
                "policy", stage.atLeast(DemoStage.R1_SPOTLIGHTING) ? ChatResource.SPOTLIGHT_POLICY : "",
                "lastToolResult", spotlighting.lastResult());
    }
}
