package com.example.meetings.model;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

public class MeetingTest {

    @Test
    void isConfirmed_NoParticipants_ReturnsFalse() {
        User organizer = new User("pedro", "pedro@email.com", "hash");
        Meeting meeting = new Meeting("Reunião", "Desc", Instant.now(), Instant.now().plusSeconds(3600), organizer);
        
        assertFalse(meeting.isConfirmed(), "Uma reunião sem participantes não pode estar confirmada.");
    }

    @Test
    void isConfirmed_AllParticipantsAccepted_ReturnsTrue() {
        User organizer = new User("pedro", "pedro@email.com", "hash");
        Meeting meeting = new Meeting("Reunião", "Desc", Instant.now(), Instant.now().plusSeconds(3600), organizer);
        
        meeting.addParticipant(new MeetingParticipant(meeting, organizer, InviteStatus.ACCEPTED));
        
        assertTrue(meeting.isConfirmed(), "Reunião deve estar confirmada se todos aceitaram.");
    }

    @Test
    void isConfirmed_MixedStatus_ReturnsFalse() {
        User organizer = new User("pedro", "pedro@email.com", "hash");
        User guest = new User("joao", "joao@email.com", "hash");
        Meeting meeting = new Meeting("Reunião", "Desc", Instant.now(), Instant.now().plusSeconds(3600), organizer);
        
        meeting.addParticipant(new MeetingParticipant(meeting, organizer, InviteStatus.ACCEPTED));
        meeting.addParticipant(new MeetingParticipant(meeting, guest, InviteStatus.PENDING));
        
        assertFalse(meeting.isConfirmed(), "Reunião não pode estar confirmada se alguém estiver pendente ou tiver declinado.");
    }
}