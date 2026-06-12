package com.example.meetings.discover;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

public class AgendaLxProviderTest {

    private WireMockServer wireMockServer;
    private AgendaLxProvider provider;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        String mockBaseUrl = "http://localhost:" + wireMockServer.port();
        provider = new AgendaLxProvider(mockBaseUrl);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void search_ValidApiCall_CleansHtmlAndParsesWeirdTimes() {
        String mockJsonResponse = """
            [
              {
                "id": 999,
                "title": { "rendered": "Exposição de Arte" },
                "description": [ "<p>Uma <b>bela</b> exposição.</p>" ],
                "occurences": [ "2026-11-05" ],
                "string_times": "qui: 21h30",
                "link": "http://agendalx.pt/arte",
                "venue": { "1": { "name": "Museu Calouste Gulbenkian" } }
              }
            ]
            """;

        stubFor(get(urlPathEqualTo("/events"))
                .withQueryParam("search", equalTo("arte"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(mockJsonResponse)));

        List<DiscoveredEvent> results = provider.search("arte");

        assertEquals(1, results.size());
        DiscoveredEvent event = results.get(0);
        assertEquals("Agenda Cultural de Lisboa", event.source());
        assertEquals("999", event.externalId());
        assertEquals("Exposição de Arte", event.title());
        
        
        assertEquals("Uma  bela  exposição.", event.description());
        assertEquals("Museu Calouste Gulbenkian", event.venue());
        
        // Verifica se a regex converteu "21h30" no dia "2026-11-05" para UTC corretamente 
        // (Em novembro PT está no mesmo fuso UTC)
        assertEquals("2026-11-05T21:30:00Z", event.start().toString());
    }
}