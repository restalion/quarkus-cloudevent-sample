package com.accenture.sample.cloudevent;

import io.quarkus.test.junit.QuarkusTest;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

@QuarkusTest
public class SampleResourceTest {

    @Test
    public void getNotAllowed() {
        given()
            .when()
                .get("/event-sample")
                .then()
                    .statusCode(405);
    }

    @Test
    public void putNotAllowed() {
        given()
            .when()
                .put("/event-sample")
                .then()
                    .statusCode(405);
    }

    @Test
    public void deleteNotAllowed() {
        given()
            .when()
                .delete("/event-sample")
                .then()
                    .statusCode(405);
    }

    @Test
    public void postWithoutBodyNotAllowed() {
        given()
            .when()
                .post("/event-sample")
                .then()
                    .statusCode(415);
    }

    @Test
    public void postWithIncorrectBodyNotAllowed() {
        given()
            .when()
                .body("{\"product\":8,\"store\":200}")
                .post("/event-sample")
                .then()
                    .statusCode(415);
    }

    @Test
    public void postWithCorrectBody() {
        given()
            .when()
                .header("Content-Type", "application/json")
                .body("{\"code\":1,\"value\":\"This is a test\"}")
                .post("/event-sample")
                .then()
                    .statusCode(200);
    }
}