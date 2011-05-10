package org.nlogo.api;

/**
 * <p>A runtime error that has occurred in NetLogo code.
 * <p/>
 * <p>As with any Exception, use the getMessage() method to get the error message.
 */

// LogoExceptions are expected to have nice end-user-understandable
// descriptions

// LogoException is abstract because engine code is supposed to throw
// a concrete subclass like EngineException. - ST 5/4/10

public abstract strictfp class LogoException
    extends Exception {

  public LogoException(String message) {
    super(message);
  }

  public LogoException(String message, Throwable cause) {
    super(message, cause);
  }

}
