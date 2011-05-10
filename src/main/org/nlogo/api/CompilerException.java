package org.nlogo.api;

/**
 * Exception thrown by various methods that accept NetLogo code as input
 * and cause that code to be compiled; indicates the code was invalid.
 * May be inspected to discover the location and nature of the error.
 */
public strictfp class CompilerException extends Exception {

  public static final String RUNTIME_ERROR_AT_COMPILE_TIME_MSG_PREFIX = "Runtime error: ";

  public CompilerException(String message, int startPos, int endPos, String fileName) {
    super(message);
    this.startPos = startPos;
    this.endPos = endPos;
    this.fileName = fileName;
  }

  public CompilerException(Token token) {
    this((String) token.value(), token.startPos(), token.endPos(), token.fileName());
    assert (token.tyype() == TokenType.BAD);
  }

  private final int startPos;

  public int startPos() {
    return startPos;
  }

  private final int endPos;

  public int endPos() {
    return endPos;
  }

  private final String fileName;

  public String fileName() {
    return fileName;
  }

  @Override
  public String toString() {
    return getMessage() + " at position " + startPos + " in " + fileName;
  }

}
