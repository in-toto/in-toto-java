package io.github.intoto.exceptions;

/** Exception thrown when there are issues validating the model. */
public class InvalidModelException extends Exception {
  public InvalidModelException(String message) {
    super(message);
  }
}
