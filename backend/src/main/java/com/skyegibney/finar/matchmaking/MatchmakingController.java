package com.skyegibney.finar.matchmaking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class MatchmakingController {
  private final MatchmakingService matchmakingService;

  @PostMapping("/lobby")
  public CreateLobbyResponse createLobby(Principal principal) {
    return new CreateLobbyResponse(matchmakingService.createLobby(principal.getName()));
  }
}
