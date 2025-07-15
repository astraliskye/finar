package com.skyegibney.finar.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
    @NotBlank String username,
    @NotBlank @Size(min = 5) String password,
    @NotBlank @Email String email) {}
