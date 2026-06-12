package com.example.meetings.repository;

import com.example.meetings.model.Meeting;
import com.example.meetings.model.User;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.List;

import static com.ninja_squad.dbsetup.Operations.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class MeetingRepositoryTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void prepareDatabase() {
        // 1. Limpar as tabelas e inserir dados de teste usando o DbSetup
        Operation operation = sequenceOf(
                deleteAllFrom("meeting_participants", "meetings", "users"), // Limpa na ordem correta devido às Foreign Keys
                insertInto("users")
                        .columns("id", "username", "email", "password_hash", "ical_token")
                        .values(1L, "pedro", "pedro@email.com", "hash", "token123")
                        .values(2L, "joao", "joao@email.com", "hash", "token456")
                        .build(),
                insertInto("meetings")
                        .columns("id", "title", "start_time", "end_time", "organizer_id")
                        // Reunião 1: Usa Instant em vez de String
                        .values(10L, "Reunião A", Instant.parse("2026-06-15T10:00:00Z"), Instant.parse("2026-06-15T11:00:00Z"), 1L)
                        // Reunião 2: Usa Instant em vez de String
                        .values(20L, "Reunião B", Instant.parse("2026-06-16T14:00:00Z"), Instant.parse("2026-06-16T15:00:00Z"), 2L)
                        .build(),
                insertInto("meeting_participants")
                        .columns("id", "meeting_id", "user_id", "status")
                        // Pedro recusa (DECLINED = ordinal 1) o convite do João
                        .values(100L, 20L, 1L, "DECLINED") 
                        .build()
        );

        // 2. Executar a operação na base de dados H2
        DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), operation);
        dbSetup.launch();
    }

    @Test
    void findCalendarMeetings_FiltersOutDeclinedInvites() {
        // Arrange
        User pedro = userRepository.findById(1L).get();

        // Act
        List<Meeting> meetings = meetingRepository.findCalendarMeetings(pedro);

        // Assert
        assertEquals(1, meetings.size(), "O Pedro só deve ter 1 reunião no calendário (a que organizou)");
        assertEquals("Reunião A", meetings.get(0).getTitle(), "A Reunião B não deve aparecer porque o Pedro a recusou");
    }

    @Test
    void findOverlapping_DetectsConflictsCorrectly() {
        // Arrange
        User pedro = userRepository.findById(1L).get();
        // Uma janela de tempo que colide com a 'Reunião A' (10:00 às 11:00)
        Instant overlapStart = Instant.parse("2026-06-15T10:30:00Z");
        Instant overlapEnd = Instant.parse("2026-06-15T11:30:00Z");

        // Act
        List<Meeting> overlapping = meetingRepository.findOverlapping(pedro, overlapStart, overlapEnd);

        // Assert
        assertEquals(1, overlapping.size(), "Deve detetar 1 reunião sobreposta");
        assertEquals("Reunião A", overlapping.get(0).getTitle());
    }
}