package com.skyegibney.finar.authorization;

import jakarta.validation.constraints.NotBlank;

record RegisterRequestDto(
    @NotBlank String username, @NotBlank String password, @NotBlank String email) {}
