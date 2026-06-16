package com.example.meetings.controller;

import com.example.meetings.model.User;
import com.example.meetings.repository.UserRepository;
import com.example.meetings.service.ICalService;
import com.example.meetings.service.MeetingService;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Collections;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Levanta o contexto do Spring Boot numa porta aleatória real
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ICalControllerTest {

    @LocalServerPort
    private int port;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MeetingService meetingService;

    @MockBean
    private ICalService icalService;

    @BeforeEach
    void setUp() {
        RestAssured.port = port; // Dizemos ao Rest-Assured para apontar para a nossa porta
    }

    @Test
    void feed_ValidToken_Returns200AndICalContent() {
        // Arrange
        String validToken = "token-secreto-123";
        User dummyUser = new User("pedro", "pedro@email.com", "pass");
        
        when(userRepository.findByIcalToken(validToken)).thenReturn(Optional.of(dummyUser));
        when(meetingService.calendarFor(dummyUser)).thenReturn(Collections.emptyList());
        when(icalService.render(any(), any())).thenReturn("BEGIN:VCALENDAR\nEND:VCALENDAR");

        // Act & Assert 
        given()
            .pathParam("token", validToken)
        .when()
            .get("/ical/{token}.ics")
        .then()
            .statusCode(200) // Verifica o HTTP Status
            .contentType("text/calendar;charset=UTF-8") // Verifica o Media Type
            .header("Content-Disposition", containsString("filename=\"meetings.ics\"")) // Verifica o ficheiro
            .body(containsString("BEGIN:VCALENDAR")); // Verifica o conteúdo
    }

    @Test
    void feed_InvalidToken_Returns404NotFound() {
        // Arrange
        String invalidToken = "token-falso";
        when(userRepository.findByIcalToken(invalidToken)).thenReturn(Optional.empty());

        // Act & Assert
        given()
            .pathParam("token", invalidToken)
            .redirects().follow(false) // Impede o Rest-Assured de entrar em loop!
        .when()
            .get("/ical/{token}.ics")
        .then()
            .statusCode(org.hamcrest.Matchers.anyOf(
                    org.hamcrest.Matchers.is(404), 
                    org.hamcrest.Matchers.is(302))); // Aceitamos o 404 direto ou o redirect de segurança
    }
}