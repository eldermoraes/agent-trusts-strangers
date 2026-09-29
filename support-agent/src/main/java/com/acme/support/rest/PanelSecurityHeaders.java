package com.acme.support.rest;

import com.acme.support.DemoStage;

import io.quarkus.vertx.http.runtime.filters.Filters;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * R4, browser side. A Vert.x filter, not a JAX-RS one: it has to cover the
 * static panel (index.html), which never goes through the REST layer.
 *
 * default-src 'self' closes images, scripts, fetch and frames to our own
 * origin; the two CDN scripts (marked, DOMPurify) are the only exception.
 */
public class PanelSecurityHeaders {

    static final String CSP = "default-src 'self'; img-src 'self'; "
            + "script-src 'self' https://cdnjs.cloudflare.com; style-src 'self' 'unsafe-inline'";

    @Inject
    DemoStage stage;

    void register(@Observes Filters filters) {
        filters.register(rc -> {
            if (stage.atLeast(DemoStage.R4_OUTPUT_GUARDRAIL)) {
                rc.response().putHeader("Content-Security-Policy", CSP);
            }
            rc.next();
        }, 100);
    }
}
