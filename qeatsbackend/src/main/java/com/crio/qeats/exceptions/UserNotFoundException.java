package com.crio.qeats.exceptions;

public class UserNotFoundException extends QEatsException {
  public UserNotFoundException(String message) {
    super(message);
  }

  @Override
  public int getErrorType() {
    return USER_NOT_FOUND;
  }
}
