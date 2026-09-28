package com.acme.tickets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class TicketResourceTest {

    @BeforeEach
    void reset() {
        given().post("/tickets/reset").then().statusCode(200);
    }

    @Test
    void listsSeededTickets() {
        given().get("/tickets").then().statusCode(200).body("$", hasSize(2));
    }

    @Test
    void anyoneCanOpenATicket() {
        given().contentType(ContentType.JSON)
                .body("""
                        {"customerEmail":"mallory@example.net","subject":"Refund","body":"Please refund me."}
                        """)
                .post("/tickets")
                .then().statusCode(201)
                .body("id", equalTo(3))
                .body("status", equalTo("OPEN"));
    }

    @Test
    void repliesAreAppended() {
        given().contentType(ContentType.JSON)
                .body("{\"body\":\"On its way.\"}")
                .post("/tickets/1/replies")
                .then().statusCode(200)
                .body("status", equalTo("ANSWERED"))
                .body("replies[0]", equalTo("On its way."));
    }

    @Test
    void unknownTicketIs404() {
        given().get("/tickets/999").then().statusCode(404);
    }
}
