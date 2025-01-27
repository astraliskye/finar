package com.skyegibney.finar.authorization;

import jakarta.validation.constraints.NotBlank;

record LoginRequestDto(@NotBlank String username, @NotBlank String password) {}
