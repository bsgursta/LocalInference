package com.proto.localinference.exceptions;

public class InvalidSocketConnectionException extends RuntimeException {

  public InvalidSocketConnectionException(SocketErrorOption opt) {
    super("Failed to validate socket connection: " + getErrorMessage(opt));
  }

  public enum SocketErrorOption {
    InvalidUuid,
    ImproperDelimiterUsed,
    MissingMcuOption,
    MissingRequiredContextLine,
    ImproperSequenceOfEvents,
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
      case SocketErrorOption.ImproperSequenceOfEvents:
        return "Provided option is not of REGISTER/REREGISTER";
      default:
        return "This shouldn't occur. Missing explicit case for opt";
    }
  }
}
