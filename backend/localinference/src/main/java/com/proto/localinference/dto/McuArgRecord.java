package com.proto.localinference.dto;

import com.proto.localinference.socket.McuCommand;

public record McuArgRecord(McuCommand command, String details) {
  @Override
  public String toString() {
    return command.toString() + " with details: " + details;
  }
}
