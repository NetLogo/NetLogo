package org.nlogo.api;

public interface CommandLogoThunk {
  /**
   * @return whether the code did a "stop" at the top level
   * @throws LogoException
   */
  boolean call() throws LogoException;
}
