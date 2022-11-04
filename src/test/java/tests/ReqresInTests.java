package tests;

import io.restassured.RestAssured;
import models.lombok.RegistrationBodyLombokModel;
import models.lombok.RegistrationResponseLombokModel;
import models.lombok.UpdateBodyLombokModel;
import models.lombok.UpdateResponseLombokModel;
import models.pojo.RegistrationBodyPojoModel;
import models.pojo.RegistrationResponsePojoModel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static specs.RegistrationSpec.registrationRequestSpec;
import static specs.RegistrationSpec.registrationResponseSpec;

public class ReqresInTests {

    @BeforeAll
    static void beforeAll() {
        RestAssured.baseURI = "https://reqres.in";
    }


    @Test
    void successfulRegistrationPlainTest() {
        String body = "{ \"email\": \"eve.holt@reqres.in\", \"password\": \"pistol\" }";

        given()
                .contentType(JSON)
                .body(body)
                .log().uri()
                .log().body()
                .when()
                .post("/api/register")
                .then()
                .log().status()
                .log().body()
                .statusCode(200)
                .body("token", is("QpwL5tke4Pnpja7X4"));
    }

    @Test
    void successfulRegistrationWithPojoModelsTest() {

        RegistrationBodyPojoModel body = new RegistrationBodyPojoModel();
        body.setEmail("eve.holt@reqres.in");
        body.setPassword("pistol");
        RegistrationResponsePojoModel response = given()
                .contentType(JSON)
                .body(body)
                .log().uri()
                .log().body()
                .when()
                .post("/api/register")
                .then()
                .log().status()
                .log().body()
                .statusCode(200)
                .extract().as(RegistrationResponsePojoModel.class);
        assertEquals("QpwL5tke4Pnpja7X4", response.getToken());
    }

    @Test
    void successfulUpdatePlainTest() {
        String body = "{ \"name\": \"neo\", \"job\": \"hacker\" }";

        given()
                .contentType(JSON)
                .body(body)
                .log().uri()
                .log().body()
                .when()
                .patch("/api/users/2")
                .then()
                .log().status()
                .log().body()
                .statusCode(200)
                .body("name", is("neo"), "job", is("hacker"));
    }

    @Test
    void successfulUpdateWithLombokModelsTest() {
        UpdateBodyLombokModel body = new UpdateBodyLombokModel();
        body.setName("neo");
        body.setJob("hacker");
        UpdateResponseLombokModel response = given()
                .contentType(JSON)
                .body(body)
                .log().uri()
                .log().body()
                .when()
                .patch("/api/users/2")
                .then()
                .log().status()
                .log().body()
                .statusCode(200)
                .extract().as(UpdateResponseLombokModel.class);

        assertThat(response.getName()).isEqualTo("neo");
        assertThat(response.getJob()).isEqualTo("hacker");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String updated = response.getUpdatedAt();
        assertThat(updated.startsWith(dtf.format(LocalDateTime.now())));
    }

    @Test
    void successfulListTest() {
        given()
                .log().uri()
                .log().body()
                .when()
                .get("/api/unknown")
                .then()
                .log().status()
                .log().body()
                .body("data[0].id", is(1));
    }

    @Test
    void negativeResourceTest() {
        given()
                .log().uri()
                .log().body()
                .when()
                .get("/api/unknown/23")
                .then()
                .log().status()
                .log().body()
                .body("isEmpty()", is(true));
    }

    @Test
    void negativeRegistrationPlainTest() {
        String body = "{ \"email\": \"eve.holt@reqres.in\" }";
        given()
                .contentType(JSON)
                .body(body)
                .log().uri()
                .log().body()
                .when()
                .post("/api/register")
                .then()
                .log().status()
                .log().body()
                .statusCode(400)
                .body("error", is("Missing password"));
    }

    @Test
    void negativeRegistrationWithSpecsTest() {
        RegistrationBodyLombokModel body = new RegistrationBodyLombokModel();
        body.setEmail("neo");
        RegistrationResponseLombokModel response = given()
                .spec(registrationRequestSpec)
                .body(body)
                .when()
                .post()
                .then()
                .spec(registrationResponseSpec)
                .extract()
                .as(RegistrationResponseLombokModel.class);
        assertThat(response.getError()).isEqualTo("Missing password");
    }

}
