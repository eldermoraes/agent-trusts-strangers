package com.acme.support;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

/** Every defense on, as at the end of the talk. */
public class AllDefensesProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("demo.stage", "5", "demo.spotlight.nonce", "true");
    }
}
