package org.nlogo.api

/**
 * Partial implementation of Reporter provides common implementations of some methods.  Implements
 * every method except <code>report(...)</code>.
 */
abstract class DefaultReporter extends Reporter {

  /**
   * Indicates that this primitive can be used by any agent.
   *
   * @return <code>"OTPL"</code>
   */
  override def getAgentClassString = "OTPL"

  /**
   * Indicates that this reporter takes no arguments and returns a number.
   *
   * @return <code>Syntax.reporterSyntax(Syntax.TYPE_NUMBER)</code>
   */
  override def getSyntax =
    Syntax.reporterSyntax(Syntax.TYPE_NUMBER)

  /**
   * Returns a new instance of this class, created by invoking the empty constructor.
   *
   * @return <code>(Reporter) this.getClass().newInstance()</code>
   */
  override def newInstance(name: String) =
    getClass.newInstance.asInstanceOf[Reporter]

}
