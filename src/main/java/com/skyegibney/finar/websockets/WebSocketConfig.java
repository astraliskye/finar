package com.skyegibney.finar.websockets;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final FinarSocketHandler finarSocketHandler;

    public WebSocketConfig(FinarSocketHandler finarSocketHandler) {
        this.finarSocketHandler = finarSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(finarSocketHandler, "/ws")
                .setAllowedOrigins("*");
    }
}
