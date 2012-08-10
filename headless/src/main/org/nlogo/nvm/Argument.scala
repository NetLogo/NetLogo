// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api
import api.{ Dump, Nobody, Syntax, TypeNames, LogoException, ExtensionException }

/**
 * Passes arguments to extension primitives.
 */
class Argument(context: Context, arg: Reporter) extends api.Argument {

  private[this] var cached: AnyRef = null

  @throws(classOf[LogoException])
  def get = {
    if (cached == null)
      cached = arg.report(context)
    cached match {
      case a: api.Agent if a.id == -1 =>
        cached = Nobody
      case _ =>
    }
    cached
  }

  @throws(classOf[ExtensionException])
  @throws(classOf[LogoException])
  def getAgentSet: api.AgentSet =
    get match {
      case agents: api.AgentSet => agents
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.AgentsetType, x))
    }

  @throws(classOf[ExtensionException])
  @throws(classOf[LogoException])
  def getAgent: api.Agent =
    get match {
      case agent: api.Agent => agent
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.AgentType, x))
    }

  @throws(classOf[ExtensionException])
  @throws(classOf[LogoException])
  def getBoolean: java.lang.Boolean =
    get match {
      case b: java.lang.Boolean => b
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.BooleanType, x))
    }

  @throws(classOf[ExtensionException])
  @throws(classOf[LogoException])
  def getBooleanValue: Boolean =
    get match {
      case b: java.lang.Boolean => b.booleanValue
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.BooleanType, x))
    }

  @throws(classOf[ExtensionException])
  @throws(classOf[LogoException])
  def getDoubleValue: Double =
    get match {
      case d: java.lang.Double => d.doubleValue
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.NumberType, x))
    }

  @throws(classOf[ExtensionException])
  @throws(classOf[LogoException])
  def getIntValue: Int =
    get match {
      case d: java.lang.Double => d.intValue
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.NumberType, x))
    }

  @throws(classOf[ExtensionException])
  @throws(classOf[LogoException])
  def getList: api.LogoList =
    get match {
      case l: api.LogoList => l
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.ListType, x))
    }

  @throws(classOf[ExtensionException])
  @throws(classOf[LogoException])
  def getPatch: api.Patch =
    get match {
      case p: api.Patch => p
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.PatchType, x))
    }

  @throws(classOf[ExtensionException])
  @throws(classOf[LogoException])
  def getString: String =
    get match {
      case s: String => s
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.StringType, x))
    }

  @throws(classOf[ExtensionException])
  @throws(classOf[LogoException])
  def getTurtle: api.Turtle =
    get match {
      case t: api.Turtle => t
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.TurtleType, x))
    }

  @throws(classOf[ExtensionException])
  @throws(classOf[LogoException])
  def getLink: api.Link =
    get match {
      case l: api.Link => l
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.LinkType, x))
    }

  @throws(classOf[ExtensionException])
  @throws(classOf[LogoException])
  def getReporterTask: api.ReporterTask =
    get match {
      case t: api.ReporterTask => t
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.ReporterTaskType, x))
    }

  @throws(classOf[ExtensionException])
  @throws(classOf[LogoException])
  def getCommandTask: api.CommandTask =
    get match {
      case t: api.CommandTask => t
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.CommandTaskType, x))
    }

  /**
   * <i>Special (undocumented) method for the cities extension.</i>
   * Returns the argument reporter without evaluating it.
   */
  def getReporter: Reporter = arg

  def getExceptionMessage(wantedType: Int, badValue: AnyRef) =
    "Expected this input to be " +
      TypeNames.aName(wantedType) + " but got " +
       (if(badValue == api.Nobody) "NOBODY"
        else ("the " + TypeNames.name(badValue) + " " +
              Dump.logoObject(badValue)) +
              " instead.")

}
