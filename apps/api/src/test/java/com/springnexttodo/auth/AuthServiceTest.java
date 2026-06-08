package com.springnexttodo.auth;

import com.springnexttodo.auth.dto.AuthResponse;
import com.springnexttodo.auth.dto.RegisterRequest;
import com.springnexttodo.tasklist.TaskList;
import com.springnexttodo.tasklist.TaskListMember;
import com.springnexttodo.tasklist.TaskListMemberRepository;
import com.springnexttodo.tasklist.TaskListRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TaskListRepository taskListRepository;
    @Mock private TaskListMemberRepository taskListMemberRepository;

    @InjectMocks
    private AuthService authService;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = new User();
        savedUser.setName("Pedro");
        savedUser.setEmail("pedro@example.com");
        savedUser.setPasswordHash("hashed");
    }

    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest("Pedro", "pedro@example.com", "secret123");
        when(userRepository.existsByEmail("pedro@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(taskListRepository.save(any(TaskList.class))).thenAnswer(inv -> inv.getArgument(0));
        when(taskListMemberRepository.save(any(TaskListMember.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.register(req);

        assertThat(response.name()).isEqualTo("Pedro");
        assertThat(response.email()).isEqualTo("pedro@example.com");
        verify(userRepository).save(any(User.class));
        verify(taskListRepository).save(any(TaskList.class));
    }

    @Test
    void register_emailAlreadyUsed() {
        RegisterRequest req = new RegisterRequest("Pedro", "pedro@example.com", "secret123");
        when(userRepository.existsByEmail("pedro@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(EmailAlreadyUsedException.class);
    }

    @Test
    void findByEmail_found() {
        when(userRepository.findByEmail("pedro@example.com")).thenReturn(Optional.of(savedUser));

        AuthResponse response = authService.findByEmail("pedro@example.com");

        assertThat(response.name()).isEqualTo("Pedro");
        assertThat(response.email()).isEqualTo("pedro@example.com");
    }

    @Test
    void findByEmail_notFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.findByEmail("missing@example.com"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
