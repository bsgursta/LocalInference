package com.proto.localinference.exceptions;

public class InvalidSocketRequestException extends RuntimeException {

  public InvalidSocketRequestException(SocketErrorOption opt) {
    super("Failed to validate socket connection: " + getErrorMessage(opt));
  }

  public enum SocketErrorOption {
    InvalidUuid,
    ImproperDelimiterUsed,
    MissingMcuOption,
    MissingRequiredContextLine,
    ImproperSequenceOfEvents,
    EOF,
  }

  private static String getErrorMessage(SocketErrorOption opt) {
    switch (opt) {
      case SocketErrorOption.InvalidUuid:
        return "Invalid UUID provided";
      case SocketErrorOption.ImproperDelimiterUsed:
        return "Missing delimiter char after UUID";
      case SocketErrorOption.MissingMcuOption:
        return "No McuOptions provided";
      case SocketErrorOption.MissingRequiredContextLine:
        return "Required payload not provided";
      case SocketErrorOption.EOF:
        return "EOF reached";
      case SocketErrorOption.ImproperSequenceOfEvents:
        return "Provided option is not of REGISTER/REREGISTER";
      default:
        return "This shouldn't occur. Missing explicit case for opt";
    }
  }
}
