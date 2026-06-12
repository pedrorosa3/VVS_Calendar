package com.example.meetings.service;

import com.example.meetings.model.User;
import com.example.meetings.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void register_NewUser_ReturnsSavedUser() {
        // Arrange
        String username = "pedro60294";
        String email = "pedro@alunos.fc.ul.pt";
        String rawPassword = "passwordForte";
        
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn("hashed_password_123");
        // O save devolve o próprio objeto que lhe foi passado
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        User savedUser = userService.register(username, email, rawPassword);

        // Assert
        assertNotNull(savedUser);
        assertEquals(username, savedUser.getUsername());
        assertEquals("hashed_password_123", savedUser.getPasswordHash());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ExistingUsername_ThrowsException() {
        // Arrange
        String username = "pedro60294";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.register(username, "outro@email.com", "pass123");
        });

        assertEquals("Username already taken", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void requireByUsername_UserExists_ReturnsUser() {
        // Arrange
        User dummyUser = new User("pedro60294", "pedro@alunos.fc.ul.pt", "hash");
        when(userRepository.findByUsername("pedro60294")).thenReturn(Optional.of(dummyUser));

        // Act
        User result = userService.requireByUsername("pedro60294");

        // Assert
        assertEquals("pedro60294", result.getUsername());
    }
}
