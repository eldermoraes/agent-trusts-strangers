package com.acme.support;

import org.eclipse.microprofile.config.ConfigProvider;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Which defenses are switched on. Read live from config so a hot-reload of
 * application.properties (or -Ddemo.stage) moves the demo to the next defense.
 */
@ApplicationScoped
public class DemoStage {

    public static final int NO_DEFENSE = 0;
    public static final int INPUT_GUARDRAIL = 1;
    public static final int SPOTLIGHTING = 2;
    public static final int LEAST_PRIVILEGE = 3;
    public static final int TOOL_GUARDRAIL = 4;
    public static final int OUTPUT_GUARDRAIL = 5;
    public static final int SCOPED_TOOLS = 6;

    public int current() {
        return ConfigProvider.getConfig().getOptionalValue("demo.stage", Integer.class).orElse(NO_DEFENSE);
    }

    public boolean atLeast(int stage) {
        return current() >= stage;
    }

    /** demo.only=N switches on that one defense alone (used to show the scoped tools by themselves). */
    public java.util.OptionalInt only() {
        var v = ConfigProvider.getConfig().getOptionalValue("demo.only", Integer.class);
        return v.map(java.util.OptionalInt::of).orElse(java.util.OptionalInt.empty());
    }

    /** Is this defense on? Cumulative by stage, unless one defense is isolated with demo.only. */
    public boolean enabled(int defense) {
        var only = only();
        return only.isPresent() ? only.getAsInt() == defense : atLeast(defense);
    }

    public boolean randomNonce() {
        return ConfigProvider.getConfig().getOptionalValue("demo.spotlight.nonce", Boolean.class).orElse(false);
    }

    public String describe() {
        var only = only();
        if (only.isPresent()) {
            return "only: " + name(only.getAsInt());
        }
        return current() == NO_DEFENSE ? "no defense" : name(current());
    }

    static String name(int defense) {
        return switch (defense) {
            case NO_DEFENSE -> "no defense";
            case INPUT_GUARDRAIL -> "input guardrail";
            case SPOTLIGHTING -> "instruction/data separation";
            case LEAST_PRIVILEGE -> "least privilege: tool allowlist";
            case TOOL_GUARDRAIL -> "tool-call guardrail";
            case OUTPUT_GUARDRAIL -> "output guardrail + CSP";
            default -> "tools scoped to the request";
        };
    }
}
