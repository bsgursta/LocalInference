package com.proto.localinference.exceptions;

public class InvalidSocketConnectionException extends RuntimeException {

  public InvalidSocketConnectionException(String errString) {
    super("Failed to validate socket connection: " + errString);
  }
}
