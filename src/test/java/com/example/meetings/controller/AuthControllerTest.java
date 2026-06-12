package com.example.meetings.controller;

import com.example.meetings.service.UserService;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {

    @LocalServerPort
    private int port;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void loginPage_IsAccessible_Returns200() {
        given()
        .when()
            .get("/login")
        .then()
            .statusCode(200);
    }

    @Test
    void registerUser_ExistingUsername_ReturnsToRegisterPageWithError() {
        // Arrange: Simulamos que o serviço lança exceção (username ocupado)
        doThrow(new IllegalArgumentException("Username already taken"))
                .when(userService).register("pedro", "pedro@email.com", "pass");

        // Act & Assert
        given()
            .formParam("username", "pedro")
            .formParam("email", "pedro@email.com")
            .formParam("password", "pass")
        .when()
            .post("/register")
        .then()
            .statusCode(302); 
    }
}
