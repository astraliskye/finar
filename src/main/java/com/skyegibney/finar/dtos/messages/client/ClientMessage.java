package com.skyegibney.finar.dtos.messages.client;

public record ClientMessage(
        String type,
        Object data
) {
}
