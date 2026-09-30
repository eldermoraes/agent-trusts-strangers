package com.acme.support.rest;

import com.acme.support.DemoStage;

import io.quarkus.vertx.http.runtime.filters.Filters;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * Output guardrail, browser side. A Vert.x filter, not a JAX-RS one: it has to cover the
 * static panel (index.html), which never goes through the REST layer.
 *
 * default-src 'self' keeps images, scripts, fetch and frames on our own origin
 * (marked and DOMPurify are served from /vendor, no CDN in the allow-list).
 * form-action is separate: it does not fall back to default-src.
 */
public class PanelSecurityHeaders {

    static final String CSP = "default-src 'self'; img-src 'self'; script-src 'self'; "
            + "style-src 'self' 'unsafe-inline'; form-action 'self'; base-uri 'none'";

    @Inject
    DemoStage stage;

    void register(@Observes Filters filters) {
        filters.register(rc -> {
            if (stage.enabled(DemoStage.OUTPUT_GUARDRAIL)) {
                rc.response().putHeader("Content-Security-Policy", CSP);
            }
            rc.next();
        }, 100);
    }
}
