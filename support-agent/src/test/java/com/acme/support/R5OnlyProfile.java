package com.acme.support;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

/** Only the last defense on: tools scoped to the request, nothing else. */
public class R5OnlyProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("demo.stage", "0", "demo.only", "6");
    }
}
