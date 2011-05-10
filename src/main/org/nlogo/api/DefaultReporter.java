package org.nlogo.api;

/**
 * Partial implementation of Reporter provides common implementations
 * of some methods.
 * Implements every method except <code>report(...)</code>.
 */
public abstract strictfp class DefaultReporter implements Reporter {

  /**
   * Indicates that this primitive can be used by any agent.
   *
   * @return <code>"OTPL"</code>
   */
  public String getAgentClassString() {
    return "OTPL";
  }

  /**
   * Indicates that this reporter takes no arguments
   * and returns a number.
   *
   * @return <code>Syntax.reporterSyntax( Syntax.TYPE_NUMBER )</code>
   */
  public Syntax getSyntax() {
    return Syntax.reporterSyntax(Syntax.TYPE_NUMBER);
  }

  /**
   * Returns a new instance of this class, created by invoking
   * the empty constructor.
   *
   * @return <code>(Reporter) this.getClass().newInstance()</code>
   */
  public Reporter newInstance(String name) {
    try {
      return getClass().newInstance();
    } catch (java.lang.InstantiationException ex) {
      throw new IllegalStateException(ex);
    } catch (java.lang.IllegalAccessException ex) {
      throw new IllegalStateException(ex);
    }
  }

}
