package com.skyegibney.finar.health;

import com.skyegibney.finar.ErrorResponse;
import com.skyegibney.finar.auth.AuthenticationService;
import com.skyegibney.finar.auth.User;
import com.skyegibney.finar.auth.dtos.LoginRequestDto;
import com.skyegibney.finar.auth.dtos.RegisterRequestDto;
import com.skyegibney.finar.auth.dtos.UserResponseDto;
import com.skyegibney.finar.auth.exceptions.DuplicateEmailException;
import com.skyegibney.finar.auth.exceptions.DuplicateUsernameException;
import com.skyegibney.finar.auth.exceptions.UnauthenticatedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequiredArgsConstructor
public class HealthController {
    @GetMapping("/health")
    public HealthResponseDto health() {
        return new HealthResponseDto("ok");
    }

}

