package com.skyegibney.finar.services;

import com.skyegibney.finar.dtos.RegisterRequestDto;
import com.skyegibney.finar.errors.DuplicateEmailException;
import com.skyegibney.finar.errors.DuplicateUsernameException;
import com.skyegibney.finar.models.User;
import com.skyegibney.finar.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
                registrationRequest.password(),
                registrationRequest.email()
        ));
    }
}
