package com.skyegibney.finar.auth;

import com.skyegibney.finar.ErrorResponse;
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
public class AuthController {
  private final AuthenticationService authenticationService;
  private final HttpSessionSecurityContextRepository sessionSecurityContextRepository;
  private final SecurityContextHolderStrategy securityContextHolder;

  @GetMapping("/me")
  public UserResponseDto me(@AuthenticationPrincipal User user) throws UnauthenticatedException {
    if (user == null) {
      throw new UnauthenticatedException("Invalid user credentials");
    }

    return new UserResponseDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new));
  }

  @PostMapping("/login")
  public void login(
      @RequestBody @Valid LoginRequestDto dto,
      HttpServletRequest request,
      HttpServletResponse response) {
    var authentication = authenticationService.login(dto);

    var context = securityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    sessionSecurityContextRepository.saveContext(context, request, response);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/register")
  public void register(
      @RequestBody @Valid RegisterRequestDto dto,
      HttpServletRequest request,
      HttpServletResponse response)
      throws DuplicateUsernameException, DuplicateEmailException {
    var authentication = authenticationService.register(dto);

    var context = securityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    sessionSecurityContextRepository.saveContext(context, request, response);
  }

  @ExceptionHandler({
    UnauthenticatedException.class,
    DuplicateEmailException.class,
    DuplicateUsernameException.class,
    BadCredentialsException.class,
  })
  public ResponseEntity<ErrorResponse> handleUnauthorized(Exception e) {
    ErrorResponse errorResponse =
        switch (e) {
          case UnauthenticatedException unauthenticated ->
              new ErrorResponse(unauthenticated.getMessage());
          case DuplicateUsernameException duplicateUsername ->
              new ErrorResponse(duplicateUsername.getMessage());
          case DuplicateEmailException duplicateEmail ->
              new ErrorResponse(duplicateEmail.getMessage());
          case BadCredentialsException badCredentials ->
              new ErrorResponse(badCredentials.getMessage());
          default -> new ErrorResponse("Unknown error.");
        };

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleForbidden() {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new ErrorResponse("You are not authorized"));
  }
}
