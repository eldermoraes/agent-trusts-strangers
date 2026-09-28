package com.acme.sink;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SinkResourceTest {

    @BeforeEach
    void clear() {
        given().delete("/log").then().statusCode(204);
    }

    @Test
    void imageBeaconIsLoggedWithItsQuery() {
        given().get("/pixel.png?d=hello").then().statusCode(200).contentType("image/gif");
        given().get("/log").then().statusCode(200)
                .body("$", hasSize(1))
                .body("[0].method", equalTo("GET"))
                .body("[0].path", equalTo("/pixel.png"))
                .body("[0].query", equalTo("d=hello"));
    }

    @Test
    void postedBodiesAreLogged() {
        given().body("to=mallory&body=secret").post("/inbox").then().statusCode(200);
        given().get("/log").then()
                .body("[0].method", equalTo("POST"))
                .body("[0].path", equalTo("/inbox"))
                .body("[0].body", equalTo("to=mallory&body=secret"));
    }
}
