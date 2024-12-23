package com.skyegibney.finar.authorization;

import com.skyegibney.finar.authorization.exceptions.DuplicateEmailException;
import com.skyegibney.finar.authorization.exceptions.DuplicateUsernameException;
import com.skyegibney.finar.authorization.exceptions.UnauthenticatedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

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
        var authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(dto.username(), dto.password());
        var authenticationResult = authenticationManager.authenticate(authenticationRequest);

        var context = securityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationResult);
        sessionSecurityContextRepository.saveContext(context, request, response);

        return authenticationResult.getPrincipal();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public Object register(@RequestBody @Valid RegisterRequestDto dto, HttpServletRequest request, HttpServletResponse response) throws DuplicateUsernameException, DuplicateEmailException {
        authenticationService.registerUser(dto);

        var authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(dto.username(), dto.password());
        var authenticationResult = authenticationManager.authenticate(authenticationRequest);

        var context = securityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationResult);
        sessionSecurityContextRepository.saveContext(context, request, response);

        return authenticationResult.getPrincipal();
    }
}
