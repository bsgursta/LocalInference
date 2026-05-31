package com.proto.localinference.exceptions;

public class UnexpectedException extends RuntimeException {

  public UnexpectedException(String context) {
    super("An unexpected behavior has occurred:\n" + context);
  }
}
