// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

// This is in Java for now because of
// issues.scala-lang.org/browse/SI-6063

public class NonLocalExit extends RuntimeException {
  // for efficiency, don't fill in stack trace
  @Override
  public RuntimeException fillInStackTrace() {
    return this;
  }
  public static final NonLocalExit singleton =
    new NonLocalExit();
}
