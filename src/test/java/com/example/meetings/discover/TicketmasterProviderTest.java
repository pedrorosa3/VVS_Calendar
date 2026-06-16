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

public class TicketmasterProviderTest {

    private WireMockServer wireMockServer;
    private TicketmasterProvider provider;

    @BeforeEach
    void setUp() {
        // Inicia o servidor Stub numa porta aleatória livre
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        // Instancia o provider redirecionando-o para o servidor Stub em vez da API real
        String mockBaseUrl = "http://localhost:" + wireMockServer.port();
        provider = new TicketmasterProvider("dummy-key", "PT", mockBaseUrl);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop(); // Desliga o servidor após cada teste
    }

    @Test
    void search_ValidApiCall_ParsesJsonResponseCorrectly() {
        // Arrange: Preparamos o JSON falso que simula a resposta da API do Ticketmaster
        String mockJsonResponse = """
            {
              "_embedded": {
                "events": [
                  {
                    "id": "Z698xZ2qZ",
                    "name": "Concerto Teste",
                    "url": "http://ticketmaster.com/concerto",
                    "info": "Informação detalhada do concerto",
                    "dates": {
                      "start": { "dateTime": "2026-10-20T20:00:00Z" }
                    },
                    "_embedded": {
                      "venues": [ { "name": "Altice Arena" } ]
                    }
                  }
                ]
              }
            }
            """;

        // Programamos o servidor para responder ao GET com o nosso JSON
        stubFor(get(urlPathEqualTo("/events.json"))
                .withQueryParam("keyword", equalTo("concerto"))
                .withQueryParam("size", equalTo("20"))
                .withQueryParam("apikey", equalTo("dummy-key"))
                .withQueryParam("countryCode", equalTo("PT"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(mockJsonResponse)));

        // Act: Fazemos a pesquisa na nossa aplicação
        List<DiscoveredEvent> results = provider.search("concerto");

        // Assert: Validamos se a nossa classe mapeou os campos corretamente
        assertEquals(1, results.size(), "Deve retornar 1 evento descoberto");
        
        DiscoveredEvent event = results.get(0);
        assertEquals("Ticketmaster", event.source());
        assertEquals("Z698xZ2qZ", event.externalId());
        assertEquals("Concerto Teste", event.title());
        assertEquals("Informação detalhada do concerto", event.description());
        assertEquals("Altice Arena", event.venue());
        assertEquals(Instant.parse("2026-10-20T20:00:00Z"), event.start());
        assertEquals("http://ticketmaster.com/concerto", event.url());
    }

    @Test
    void search_ApiReturns500Error_ReturnsEmptyListAndSwallowsException() {
        // Arrange: Simulamos que o Ticketmaster foi abaixo (Internal Server Error)
        stubFor(get(urlPathEqualTo("/events.json"))
                .willReturn(aResponse().withStatus(500)));

        // Act
        List<DiscoveredEvent> results = provider.search("qualquer coisa");

        // Assert: O método deve engolir o erro graciosamente e devolver lista vazia
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Deve devolver uma lista vazia em caso de falha da API");
    }
}