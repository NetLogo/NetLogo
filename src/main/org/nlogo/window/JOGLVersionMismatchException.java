package org.nlogo.window;

public strictfp class JOGLVersionMismatchException
    extends JOGLLoadingException {
  public JOGLVersionMismatchException(String message) {
    super(message);
  }
}
