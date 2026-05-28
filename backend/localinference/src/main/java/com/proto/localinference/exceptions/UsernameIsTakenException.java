package com.proto.localinference.exceptions;

public class UsernameIsTakenException extends RuntimeException {

  public UsernameIsTakenException(String username) {
    super("Username " + username + " is already in use. Try another one.");
  }
}
