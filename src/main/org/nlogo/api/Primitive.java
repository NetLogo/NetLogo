package org.nlogo.api;

/**
 * Top-level interface for primitives (commands or reporters).
 * Not to be implemented directly; the <code>Command</code>
 * or <code>Reporter</code> interface should be used instead.
 *
 * @see Command
 * @see Reporter
 */
public interface Primitive {
  /**
   * Returns a String which specifies in which context this
   * primitive is allowed.  To specify observer use "O", to specify
   * Turtle use "T", and to specify Patch use "P".  To use combinations,
   * put them togther.
   * <p/>
   * Examples:
   * <p>For a primitive that is allowed in all contexts,
   * <pre> String getAgentClassString() { return "OTP"; }</pre>
   * <p>For a primitive that is allowed only in a turtle context,
   * <pre> String getAgentClassString() { return "T"; }</pre>
   *
   * @return a String specifying the acceptable context.
   */
  String getAgentClassString();

  /**
   * Returns Syntax which specifies the syntax that is acceptable for
   * this primitive.  Used by the compiler for type-checking.
   *
   * @return the Syntax for the primitive.
   * @see Syntax
   */
  Syntax getSyntax();
}
