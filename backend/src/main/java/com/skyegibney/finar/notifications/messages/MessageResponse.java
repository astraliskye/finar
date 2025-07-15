package com.skyegibney.finar.notifications.messages;

public record MessageResponse(String type, Object data) {
  public MessageResponse(String type) {
    this(type, null);
  }
}
