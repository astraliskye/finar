package com.skyegibney.finar.authorization;

import com.skyegibney.finar.authorization.exceptions.DuplicateEmailException;
import com.skyegibney.finar.authorization.exceptions.DuplicateUsernameException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(RegisterRequestDto registrationRequest) throws DuplicateUsernameException, DuplicateEmailException {
        var nameCheck = userRepository.findByUsername(registrationRequest.username());
        if (nameCheck != null) {
            throw new DuplicateUsernameException();
        }

        var emailCheck = userRepository.findByEmail(registrationRequest.email());
        if (emailCheck != null) {
            throw new DuplicateEmailException();
        }

        return userRepository.save(new User(
                0,
                registrationRequest.username(),
                passwordEncoder.encode(registrationRequest.password()),
                registrationRequest.email()
        ));
    }
}
