package com.example.meetings.discover;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

public class SeatGeekProviderTest {

    private WireMockServer wireMockServer;
    private SeatGeekProvider provider;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        String mockBaseUrl = "http://localhost:" + wireMockServer.port();
        provider = new SeatGeekProvider("dummy-client-id", mockBaseUrl);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void search_ValidApiCall_ParsesSeatGeekJsonCorrectly() {
        String mockJsonResponse = """
            {
              "events": [
                {
                  "id": 12345,
                  "title": "Jogo de Futebol",
                  "datetime_utc": "2026-08-10T18:30:00",
                  "url": "https://seatgeek.com/jogo",
                  "description": "Bancada Central",
                  "venue": { "name": "Estádio da Luz" }
                }
              ]
            }
            """;

        stubFor(get(urlPathEqualTo("/events"))
                .withQueryParam("q", equalTo("futebol"))
                .withQueryParam("client_id", equalTo("dummy-client-id"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(mockJsonResponse)));

        List<DiscoveredEvent> results = provider.search("futebol");

        assertEquals(1, results.size());
        DiscoveredEvent event = results.get(0);
        assertEquals("SeatGeek", event.source());
        assertEquals("12345", event.externalId());
        assertEquals("Jogo de Futebol", event.title());
        assertEquals("Estádio da Luz", event.venue());
        assertEquals(Instant.parse("2026-08-10T18:30:00Z"), event.start());
    }
}