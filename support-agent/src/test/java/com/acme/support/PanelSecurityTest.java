package com.acme.support;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

/** With every defense on: the CSP must reach the static panel, not only the REST layer. */
@QuarkusTest
@TestProfile(AllDefensesProfile.class)
class PanelSecurityTest {

    @Test
    void cspReachesTheStaticPanel() {
        given().get("/").then().statusCode(200)
                .header("Content-Security-Policy", containsString("default-src 'self'"));
        given().get("/panel.js").then().statusCode(200)
                .header("Content-Security-Policy", containsString("img-src 'self'"));
    }

    @Test
    void cspReachesTheApiToo() {
        given().get("/api/stage").then().statusCode(200)
                .header("Content-Security-Policy", containsString("default-src 'self'"));
    }

    @Test
    void r5_theServerDecidesWhichTicketARequestIsAbout() {
        assertEquals(Optional.of(2), CurrentTicket.parse("Please handle ticket #2."));
        assertEquals(Optional.of(17), CurrentTicket.parse("look at #17 please"));
        assertTrue(CurrentTicket.parse("Summarize everything").isEmpty());
    }
}
