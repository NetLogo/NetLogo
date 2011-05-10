package org.nlogo.api;

/**
 * Partial implementation of Command provides common implentations
 * of some methods.
 * Implements every method except <code>Command.perform(...)</code>.
 */
public abstract strictfp class DefaultCommand implements Command {

  /**
   * Indicates that this primitive can be used by any agent.
   *
   * @return <code>"OTPL"</code>
   */
  public String getAgentClassString() {
    return "OTPL";
  }

  /**
   * Indicates that this command takes no arguments.
   *
   * @return <code>Syntax.commandSyntax()</code>
   */
  public Syntax getSyntax() {
    return Syntax.commandSyntax();
  }

  /**
   * Indicates that NetLogo does not need to switch
   * agents after executing this command.
   */
  public boolean getSwitchesBoolean() {
    return false;
  }

  /**
   * Returns a new instance of this class, created by invoking
   * the empty constructor.
   *
   * @return <code>(Command) this.getClass().newInstance()</code>
   */
  public Command newInstance(String name) {
    try {
      return getClass().newInstance();
    } catch (java.lang.InstantiationException ex) {
      throw new IllegalStateException(ex);
    } catch (java.lang.IllegalAccessException ex) {
      throw new IllegalStateException(ex);
    }
  }

}
