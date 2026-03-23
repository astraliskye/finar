package com.skyegibney.finar.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyegibney.finar.auth.dtos.LoginRequestDto;
import com.skyegibney.finar.auth.dtos.RegisterRequestDto;
import com.skyegibney.finar.auth.exceptions.DuplicateEmailException;
import com.skyegibney.finar.auth.exceptions.DuplicateUsernameException;
import com.skyegibney.finar.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test for {@link AuthController}.
 *
 * <p>We import the real {@link SecurityConfig} so that the custom
 * {@code permitAll()} rules for /login, /register, and /me are active and
 * CSRF is disabled – matching production behaviour. Only
 * {@link CustomUserDetailsService} (needed by SecurityConfig's
 * AuthenticationManager) and {@link AuthenticationService} are mocked.
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    /** Required by SecurityConfig.authenticationManager(). */
    @MockBean
    CustomUserDetailsService customUserDetailsService;

    @MockBean
    AuthenticationService authenticationService;

    // -------------------------------------------------------------------------
    // GET /me
    // -------------------------------------------------------------------------

    @Test
    void me_authenticated_returnsUserInfo() throws Exception {
        User principal = new User(1L, "alice", "hashed", "alice@example.com", null);

        // user() post-processor sets the SecurityContext so @AuthenticationPrincipal resolves.
        mockMvc.perform(get("/me").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void me_unauthenticated_returns401() throws Exception {
        // /me is permitAll() in SecurityConfig, so the request reaches the controller
        // as an unauthenticated user. The controller throws UnauthenticatedException
        // → handled by the @ExceptionHandler → 401.
        mockMvc.perform(get("/me"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // POST /login
    // -------------------------------------------------------------------------

    @Test
    void login_validCredentials_returns200() throws Exception {
        var dto = new LoginRequestDto("alice", "password123");
        var auth = new UsernamePasswordAuthenticationToken("alice", "password123", List.of());

        when(authenticationService.login(any())).thenReturn(auth);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        var dto = new LoginRequestDto("alice", "wrongpass");

        when(authenticationService.login(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_missingUsername_returns400() throws Exception {
        String body = "{\"username\":\"\",\"password\":\"password123\"}";

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // POST /register
    // -------------------------------------------------------------------------

    @Test
    void register_newUser_returns201() throws Exception {
        var dto = new RegisterRequestDto("alice", "password123", "alice@example.com");
        var auth = new UsernamePasswordAuthenticationToken("alice", "password123", List.of());

        when(authenticationService.register(any())).thenReturn(auth);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void register_duplicateUsername_returns401WithMessage() throws Exception {
        var dto = new RegisterRequestDto("alice", "password123", "alice@example.com");

        when(authenticationService.register(any()))
                .thenThrow(new DuplicateUsernameException("Username already exists"));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    @Test
    void register_duplicateEmail_returns401WithMessage() throws Exception {
        var dto = new RegisterRequestDto("alice", "password123", "alice@example.com");

        when(authenticationService.register(any()))
                .thenThrow(new DuplicateEmailException("Email already exists"));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        String body = "{\"username\":\"alice\",\"password\":\"password123\",\"email\":\"notanemail\"}";

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shortPassword_returns400() throws Exception {
        String body = "{\"username\":\"alice\",\"password\":\"abc\",\"email\":\"alice@example.com\"}";

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
