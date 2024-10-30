package com.skyegibney.finar.controllers;

import com.skyegibney.finar.dtos.LoginRequestDto;
import com.skyegibney.finar.dtos.RegisterRequestDto;
import com.skyegibney.finar.errors.DuplicateEmailException;
import com.skyegibney.finar.errors.DuplicateUsernameException;
import com.skyegibney.finar.errors.UnauthenticatedException;
import com.skyegibney.finar.services.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final AuthenticationService authenticationService;
    private final HttpSessionSecurityContextRepository sessionSecurityContextRepository;
    private final SecurityContextHolderStrategy securityContextHolder;

    public AuthController(AuthenticationManager authenticationManager, AuthenticationService authenticationService, HttpSessionSecurityContextRepository sessionSecurityContextRepository, SecurityContextHolderStrategy securityContextHolder) {
        this.authenticationManager = authenticationManager;
        this.authenticationService = authenticationService;
        this.sessionSecurityContextRepository = sessionSecurityContextRepository;
        this.securityContextHolder = securityContextHolder;
    }

    @GetMapping("/me")
    public Principal me(Principal user) throws UnauthenticatedException {
        if (user == null) {
            throw new UnauthenticatedException();
        }

        return user;
    }

    @PostMapping("/login")
    public Object login(@RequestBody @Valid LoginRequestDto dto, HttpServletRequest request, HttpServletResponse response) {
        Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(dto.username(), dto.password());
        Authentication authenticationResult = authenticationManager.authenticate(authenticationRequest);

        SecurityContext context = securityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationResult);
        sessionSecurityContextRepository.saveContext(context, request, response);

        return authenticationResult.getPrincipal();
    }

    @PostMapping("/register")
    public Principal register(@RequestBody @Valid RegisterRequestDto dto) throws DuplicateUsernameException, DuplicateEmailException {
        authenticationService.registerUser(dto);

        Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(dto.username(), dto.password());
        Authentication authenticationResult = authenticationManager.authenticate(authenticationRequest);
        return (Principal)authenticationResult.getPrincipal();
    }
}
