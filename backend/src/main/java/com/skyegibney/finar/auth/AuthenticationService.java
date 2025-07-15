package com.skyegibney.finar.auth;

import com.skyegibney.finar.auth.dtos.LoginRequestDto;
import com.skyegibney.finar.auth.dtos.RegisterRequestDto;
import com.skyegibney.finar.auth.exceptions.DuplicateEmailException;
import com.skyegibney.finar.auth.exceptions.DuplicateUsernameException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  public Authentication register(RegisterRequestDto registrationRequest)
      throws DuplicateUsernameException, DuplicateEmailException {
    var nameCheck = userRepository.findByUsername(registrationRequest.username());
    if (nameCheck.isPresent()) {
      throw new DuplicateUsernameException("Username already exists");
    }

    var emailCheck = userRepository.findByEmail(registrationRequest.email());
    if (emailCheck.isPresent()) {
      throw new DuplicateEmailException("Email already exists");
    }

    userRepository.save(
        new User(
            0,
            registrationRequest.username(),
            passwordEncoder.encode(registrationRequest.password()),
            registrationRequest.email(),
            null));

    return authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            registrationRequest.username(), registrationRequest.password()));
  }

  public Authentication login(LoginRequestDto loginRequest) {
    return authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));
  }
}
