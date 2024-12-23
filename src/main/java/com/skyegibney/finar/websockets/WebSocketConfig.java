package com.skyegibney.finar.websockets;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final MatchmakingHandler matchmakingHandler;
    private final PlayHandler playHandler;

    public WebSocketConfig(MatchmakingHandler matchmakingHandler, PlayHandler playHandler) {
        this.matchmakingHandler = matchmakingHandler;
        this.playHandler = playHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(matchmakingHandler, "/matchmaking")
                .setAllowedOrigins("*");

        registry
                .addHandler(playHandler, "/play")
                .setAllowedOrigins("*");
    }
}
