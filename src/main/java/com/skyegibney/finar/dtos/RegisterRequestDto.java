package com.skyegibney.finar.dtos;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDto (
        @NotBlank
        String username,
        @NotBlank
        String password,
        @NotBlank
        String confirmPassword,
        @NotBlank
        String email
) {
}
