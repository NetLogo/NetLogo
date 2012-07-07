// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.LogoException
import org.nlogo.agent.Agent

/**
 * A stack trace is displayed to the user when an error occurs in running code.
 * Example:
 *
 * division by zero
 * error while observer running /
 *   called by plot 'plot 1' setup code
 *   called by RESET-TICKS
 *   called by procedure SETUP
 *   called by Command Center
 *
 * Entries in stack traces can come from different places.
 *
 * In the example,
 * - "/" is the individual primitive in which the error occurred
 * - "plot 'plot 1' setup code" is an anonymous procedure in a plot
 * - "RESET-TICKS" is not a procedure, but a command that can trigger
 *   the execution of procedures (see also tick, setup-plots, update-plots)
 * - "SETUP" is an ordinary procedure
 * - "Command Center" is the display name of the dummy top level procedure
 *   wrapped around the user's code.
 */
object StackTraceBuilder {

  def build(act: Activation, agent: Agent, instruction: Instruction, cause: Option[Throwable]): String = {
    val errorMessage = cause map {
      case l: LogoException =>
        l.getMessage + "\nerror while "
      case e =>
        "error (" + e.getClass.getSimpleName + ")\n while "
    }
    errorMessage.getOrElse("") + agent + " running " + instruction.displayName + "\n" +
      entries(act).map("  called by " + _).mkString("\n")
  }

  private def entries(act: Activation): List[String] = {
    val activations = Iterator.iterate(act)(_.parent).takeWhile(_ != null).toList
    // flatMap because each activation can result in 1 or 2 entries
    activations.flatMap{a => a.procedure.displayName :: commandName(a).toList }
  }

  private def commandName(act: Activation): Option[String] =
    for{p <- Option(act.parent)
        if(p.procedure.code.isDefinedAt(act.returnAddress - 1))
        c = p.procedure.code(act.returnAddress - 1)
        if c.callsOtherCode}
    yield c.displayName
}
