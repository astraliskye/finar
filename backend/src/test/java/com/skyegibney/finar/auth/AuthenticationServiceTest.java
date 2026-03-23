package com.skyegibney.finar.auth;

import com.skyegibney.finar.auth.dtos.LoginRequestDto;
import com.skyegibney.finar.auth.dtos.RegisterRequestDto;
import com.skyegibney.finar.auth.exceptions.DuplicateEmailException;
import com.skyegibney.finar.auth.exceptions.DuplicateUsernameException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    AuthenticationManager authenticationManager;

    @InjectMocks
    AuthenticationService authenticationService;

    // -------------------------------------------------------------------------
    // register
    // -------------------------------------------------------------------------

    @Test
    void register_newUser_savesUserWithEncodedPassword() throws Exception {
        var dto = new RegisterRequestDto("alice", "password123", "alice@example.com");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("alice", "password123"));

        authenticationService.register(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getPassword()).isEqualTo("hashed_password");
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void register_newUser_callsAuthenticationManagerAfterSave() throws Exception {
        var dto = new RegisterRequestDto("alice", "password123", "alice@example.com");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("alice", "password123"));

        authenticationService.register(dto);

        // Authentication must use the plain-text password (not the hash) so that
        // the AuthenticationManager can verify it against the freshly saved record.
        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getName()).isEqualTo("alice");
        assertThat(tokenCaptor.getValue().getCredentials()).isEqualTo("password123");
    }

    @Test
    void register_duplicateUsername_throwsDuplicateUsernameException() {
        var dto = new RegisterRequestDto("alice", "password123", "alice@example.com");
        var existing = new User(1L, "alice", "hashed", "other@example.com", null);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> authenticationService.register(dto))
                .isInstanceOf(DuplicateUsernameException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateEmail_throwsDuplicateEmailException() {
        var dto = new RegisterRequestDto("alice", "password123", "alice@example.com");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(new User(2L, "other", "hashed", "alice@example.com", null)));

        assertThatThrownBy(() -> authenticationService.register(dto))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // login
    // -------------------------------------------------------------------------

    @Test
    void login_validCredentials_returnsAuthentication() {
        var dto = new LoginRequestDto("alice", "password123");
        var expectedAuth = new UsernamePasswordAuthenticationToken("alice", "password123");

        when(authenticationManager.authenticate(any())).thenReturn(expectedAuth);

        var result = authenticationService.login(dto);

        assertThat(result).isEqualTo(expectedAuth);
    }

    @Test
    void login_invalidCredentials_propagatesBadCredentialsException() {
        var dto = new LoginRequestDto("alice", "wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authenticationService.login(dto))
                .isInstanceOf(BadCredentialsException.class);
    }
}
