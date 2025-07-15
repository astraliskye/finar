package com.skyegibney.finar.auth.dtos;

public record UserResponseDto(
        long id,
        String username,
        String email,
        String[] roles
) {}
