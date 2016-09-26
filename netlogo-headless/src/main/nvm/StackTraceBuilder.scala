// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.{ Activation => ApiActivation, Agent, LogoException }

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
  def build(act: ApiActivation, agent: Agent, instruction: Instruction, exception: Option[Throwable]): String =
    build(act, agent, instruction, exception, null)

  def build(act: ApiActivation, agent: Agent, instruction: Instruction, exception: Option[Throwable], message: String): String = {
    val errorMessage = exception map {
      case l: LogoException =>
        Option(l.getMessage).getOrElse(message) + "\nerror while "
      case soe: StackOverflowError =>
        s"error while "
      case e =>
        s"error (${e.getClass.getSimpleName})\n while "
    }
    val instructionName = Option(instruction).map(_.displayName).getOrElse("")
    errorMessage.getOrElse("") + agent + " running " + instructionName + "\n" + buildTrace(act)
  }

  def buildTrace(act: ApiActivation): String = {
    entries(act).map("  called by " + _).mkString("\n")
  }

  private def entries(act: ApiActivation): List[String] = {
    val activations = Iterator.iterate(act)(_.parent.orNull).takeWhile(_ != null).toList
    // flatMap because each activation can result in 1 or 2 entries
    activations.flatMap{a => a.procedure.displayName :: commandName(a).toList }
  }

  private def commandName(act: ApiActivation): Option[String] =
    act match {
      case nvmActivation: Activation =>
        for {
          p <- nvmActivation.parent
            if (p.procedure.code.isDefinedAt(nvmActivation.returnAddress - 1))
          c = p.procedure.code(nvmActivation.returnAddress - 1)
            if c.callsOtherCode
        } yield c.displayName
      case _ => None
    }
}
