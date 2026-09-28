package com.acme.support;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acme.support.ai.SupportAgent;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

/** Boots the container: proves the AI service, tools, guardrails and REST layer wire up. No model needed. */
@QuarkusTest
class WiringTest {

    @Inject
    SupportAgent agent;

    @Test
    void aiServiceIsBuilt() {
        assertNotNull(agent);
    }

    @Test
    void stageEndpointReportsTheDefaultStage() {
        given().get("/api/stage").then().statusCode(200).body("stage", equalTo(0));
    }
}
