package com.example.meetings.discover;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DiscoveryServiceTest {

    @Mock
    private EventProvider provider1;

    @Mock
    private EventProvider provider2;

    @Mock
    private EventProvider unconfiguredProvider;

    private DiscoveryService discoveryService;

    @BeforeEach
    void setUp() {
        // Injetamos os nossos providers falsos no serviço
        discoveryService = new DiscoveryService(List.of(provider1, provider2, unconfiguredProvider));
    }

    @Test
    void search_DedupesByUrlAndSortsChronologically() {
        // Arrange
        when(unconfiguredProvider.isConfigured()).thenReturn(false);
        when(provider1.isConfigured()).thenReturn(true);
        when(provider2.isConfigured()).thenReturn(true);

        Instant time1 = Instant.parse("2026-08-10T10:00:00Z");
        Instant time2 = Instant.parse("2026-08-10T12:00:00Z");
        Instant time3 = Instant.parse("2026-08-10T09:00:00Z");

        // Criamos eventos falsos. O Evento A e o A_Dup partilham o mesmo URL!
        DiscoveredEvent eventA = new DiscoveredEvent("Prov1", "1", "Concerto A", "Desc", time1, null, "http://same.url", "Venue");
        DiscoveredEvent eventB = new DiscoveredEvent("Prov1", "2", "Concerto B", "Desc", time2, null, "http://other.url", "Venue");
        DiscoveredEvent eventA_Dup = new DiscoveredEvent("Prov2", "99", "Concerto A Duplicado", "Desc", time1, null, "http://same.url", "Venue");
        DiscoveredEvent eventC = new DiscoveredEvent("Prov2", "3", "Concerto C", "Desc", time3, null, "http://unique.url", "Venue");

        when(provider1.search("query")).thenReturn(List.of(eventA, eventB));
        when(provider2.search("query")).thenReturn(List.of(eventA_Dup, eventC));

        // Act
        List<DiscoveredEvent> results = discoveryService.search("query");

        // Assert
        assertEquals(3, results.size(), "Deve remover 1 duplicado (baseado no URL)");
        
        // Verifica a ordenação cronológica (time3 < time1 < time2)
        assertEquals("Concerto C", results.get(0).title(), "Concerto C é às 09:00");
        assertEquals("Concerto A", results.get(1).title(), "Concerto A é às 10:00");
        assertEquals("Concerto B", results.get(2).title(), "Concerto B é às 12:00");

        // Verifica que o unconfiguredProvider nunca foi chamado para fazer pesquisa, poupando recursos
        verify(unconfiguredProvider, never()).search(anyString());
    }
    
    @Test
    void search_NullOrBlankQuery_ReturnsEmptyList() {
        // Act
        List<DiscoveredEvent> resultsNull = discoveryService.search(null);
        List<DiscoveredEvent> resultsBlank = discoveryService.search("   ");

        // Assert
        assertTrue(resultsNull.isEmpty());
        assertTrue(resultsBlank.isEmpty());
        // Garante que não chamou nenhum provider se a query for vazia
        verify(provider1, never()).search(anyString()); 
    }
}
