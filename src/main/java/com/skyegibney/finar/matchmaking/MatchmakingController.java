package com.skyegibney.finar.matchmaking;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Random;

@RestController
public class MatchmakingController {
    private final MatchmakingService matchmakingService;

    public MatchmakingController(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    @PostMapping("/lobby")
    public CreateLobbyResponse createLobby(Principal principal, HttpServletRequest request, HttpServletResponse response) {
        return new CreateLobbyResponse(
                matchmakingService.createLobby(principal.getName())
        );
    }
}
