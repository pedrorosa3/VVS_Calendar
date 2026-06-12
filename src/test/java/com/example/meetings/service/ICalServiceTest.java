package com.example.meetings.service;

import com.example.meetings.model.InviteStatus;
import com.example.meetings.model.Meeting;
import com.example.meetings.model.MeetingParticipant;
import com.example.meetings.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ICalServiceTest {

    private ICalService iCalService;
    private User testUser;

    @BeforeEach
    void setUp() {
        iCalService = new ICalService();
        testUser = new User("pedro60294", "pedro@alunos.fc.ul.pt", "hash");
    }

    @Test
    void render_ValidMeeting_GeneratesCorrectICalFormat() {
        // Arrange
        Instant start = Instant.parse("2026-06-15T10:00:00Z");
        Instant end = Instant.parse("2026-06-15T11:00:00Z");
        Meeting meeting = new Meeting("Defesa do Projeto", "Sala 3.2.14", start, end, testUser);
        
        // Adicionar o participante como ACEITE para a reunião ficar CONFIRMED
        meeting.addParticipant(new MeetingParticipant(meeting, testUser, InviteStatus.ACCEPTED));

        // Act
        String iCalData = iCalService.render(testUser, List.of(meeting));

        // Assert
        assertNotNull(iCalData);
        assertTrue(iCalData.startsWith("BEGIN:VCALENDAR\r\n"));
        assertTrue(iCalData.contains("VERSION:2.0"));
        assertTrue(iCalData.contains("SUMMARY:Defesa do Projeto"));
        assertTrue(iCalData.contains("DESCRIPTION:Sala 3.2.14"));
        assertTrue(iCalData.contains("STATUS:CONFIRMED"));
        assertTrue(iCalData.contains("DTSTART:20260615T100000Z"));
        assertTrue(iCalData.endsWith("END:VCALENDAR\r\n"));
    }

    @Test
    void render_PendingMeeting_ShowsAsTentative() {
        // Arrange
        Instant start = Instant.parse("2026-06-16T14:00:00Z");
        Instant end = Instant.parse("2026-06-16T15:00:00Z");
        Meeting meeting = new Meeting("Reunião de Grupo", null, start, end, testUser);
        
        // Adicionar participante como PENDENTE
        meeting.addParticipant(new MeetingParticipant(meeting, testUser, InviteStatus.PENDING));

        // Act
        String iCalData = iCalService.render(testUser, List.of(meeting));

        // Assert
        assertTrue(iCalData.contains("STATUS:TENTATIVE"));
        assertTrue(iCalData.contains("PARTSTAT=NEEDS-ACTION"));
    }
}
