package com.acme.support;

import org.eclipse.microprofile.config.ConfigProvider;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Which defenses are switched on. Read live from config so a hot-reload of
 * application.properties (or -Ddemo.stage) moves the demo to the next round.
 */
@ApplicationScoped
public class DemoStage {

    public static final int NO_DEFENSE = 0;
    public static final int R0_INPUT_GUARDRAIL = 1;
    public static final int R1_SPOTLIGHTING = 2;
    public static final int R2_LEAST_PRIVILEGE = 3;
    public static final int R3_TOOL_GUARDRAIL = 4;
    public static final int R4_OUTPUT_GUARDRAIL = 5;

    public int current() {
        return ConfigProvider.getConfig().getOptionalValue("demo.stage", Integer.class).orElse(NO_DEFENSE);
    }

    public boolean atLeast(int stage) {
        return current() >= stage;
    }

    public boolean randomNonce() {
        return ConfigProvider.getConfig().getOptionalValue("demo.spotlight.nonce", Boolean.class).orElse(false);
    }

    public String describe() {
        return switch (current()) {
            case NO_DEFENSE -> "stage 0 · trusting agent, no defense";
            case R0_INPUT_GUARDRAIL -> "stage 1 · R0 input guardrail";
            case R1_SPOTLIGHTING -> "stage 2 · R1 spotlighting (" + (randomNonce() ? "random nonce" : "fixed delimiter") + ")";
            case R2_LEAST_PRIVILEGE -> "stage 3 · R2 least privilege";
            case R3_TOOL_GUARDRAIL -> "stage 4 · R3 tool input guardrail";
            default -> "stage 5 · R4 output guardrail + CSP";
        };
    }
}
