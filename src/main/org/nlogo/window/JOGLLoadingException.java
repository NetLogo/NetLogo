// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

public strictfp class JOGLLoadingException
    extends Exception {
  private Throwable cause = null;

  public JOGLLoadingException(String message) {
    super(message);
  }

  public JOGLLoadingException(String message, Throwable cause) {
    super(message);
    this.cause = cause;
  }

  @Override
  public Throwable getCause() {
    return cause;
  }
}
