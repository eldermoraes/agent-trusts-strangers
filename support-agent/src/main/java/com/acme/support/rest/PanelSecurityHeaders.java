package com.acme.support.rest;

import org.jboss.resteasy.reactive.server.ServerResponseFilter;

import com.acme.support.DemoStage;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerResponseContext;

/**
 * R4, browser side: even if some Markdown slipped through, the panel itself
 * refuses to load images from anywhere but our own origin.
 */
public class PanelSecurityHeaders {

    @Inject
    DemoStage stage;

    @ServerResponseFilter
    public void addCsp(ContainerResponseContext response) {
        if (stage.atLeast(DemoStage.R4_OUTPUT_GUARDRAIL)) {
            response.getHeaders().putSingle("Content-Security-Policy", "img-src 'self'");
        }
    }
}
