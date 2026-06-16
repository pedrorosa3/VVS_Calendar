package com.example.meetings.service;

import com.example.meetings.model.Meeting;
import com.example.meetings.model.User;
import com.example.meetings.repository.MeetingParticipantRepository;
import com.example.meetings.repository.MeetingRepository;
import com.example.meetings.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private MeetingParticipantRepository participantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MeetingService meetingService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("pedro", "pedro@example.com", "password123");
    }

    @Test
    void propose_DuplicateEvent_ThrowsException() {
        // Preparação (Arrange)
        String title = "Reunião de Projeto";
        Instant start = Instant.parse("2026-06-15T10:00:00Z");
        Instant end = Instant.parse("2026-06-15T11:00:00Z");
        
        // Simulamos que a base de dados já tem um evento com este título e data para este utilizador
        Meeting existingMeeting = new Meeting(title, "Descrição antiga", start, end, testUser);
        when(meetingRepository.findCalendarMeetings(testUser)).thenReturn(List.of(existingMeeting));

        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            meetingService.propose(testUser, title, "Nova descrição", start, end, Collections.emptyList());
        });

        assertEquals("O utilizador já possui um evento com este título a esta hora.", exception.getMessage());
        
        // Garantir que o repositório nunca chegou a guardar o duplicado
        verify(meetingRepository, never()).save(any(Meeting.class));
    }
}
