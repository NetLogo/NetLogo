package org.nlogo.api;

/**
 * Interface for objects which provide constraints for values.
 */
public interface ValueConstraint {

  /**
   * Throws a Violation condition if the input is not acceptable.
   */
  void assertConstraint(Object val) throws Violation, LogoException;

  /**
   * Returns the constrained value, which can differ from the input.
   * Throws a Violation condition if the input is not coercable.
   */
  Object coerceValue(Object val) throws Violation, LogoException;

  /**
   * Returns the default value for this constraint
   */
  Object defaultValue();

  //void updateConstraint() throws CompilerException, LogoException ;

  class Violation extends AgentException {
    public Violation(String message) {
      super(message);
    }
  }
}
