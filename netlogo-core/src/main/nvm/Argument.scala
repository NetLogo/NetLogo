// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.{ api, core },
  core.{ Nobody, Reference, Syntax, Token, TypeNames },
  api.{ Dump, ExtensionException, LogoException }

import java.util.{ List => JList }

import scala.unchecked

/**
 * Passes arguments to extension primitives.
 */
class Argument(context: Context, arg: Reporter) extends api.Argument {

  private[this] var cached: AnyRef = null

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
  def getAgentSet: api.AgentSet =
    get match {
      case agents: api.AgentSet => agents
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.AgentsetType, x))
    }

  @throws(classOf[ExtensionException])
  def getAgent: api.Agent =
    get match {
      case agent: api.Agent => agent
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.AgentType, x))
    }

  @throws(classOf[ExtensionException])
  def getBoolean: java.lang.Boolean =
    get match {
      case b: java.lang.Boolean => b
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.BooleanType, x))
    }

  @throws(classOf[ExtensionException])
  def getBooleanValue: Boolean =
    get match {
      case b: java.lang.Boolean => b.booleanValue
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.BooleanType, x))
    }

  @throws(classOf[ExtensionException])
  def getDoubleValue: Double =
    get match {
      case d: java.lang.Double => d.doubleValue
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.NumberType, x))
    }

  @throws(classOf[ExtensionException])
  def getIntValue: Int =
    get match {
      case d: java.lang.Double => d.intValue
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.NumberType, x))
    }

  @throws(classOf[ExtensionException])
  def getList: core.LogoList =
    get match {
      case l: core.LogoList => l
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.ListType, x))
    }

  @throws(classOf[ExtensionException])
  def getPatch: api.Patch =
    get match {
      case p: api.Patch => p
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.PatchType, x))
    }

  @throws(classOf[ExtensionException])
  def getString: String =
    get match {
      case s: String => s
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.StringType, x))
    }

  @throws(classOf[ExtensionException])
  def getTurtle: api.Turtle =
    get match {
      case t: api.Turtle => t
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.TurtleType, x))
    }

  @throws(classOf[ExtensionException])
  def getLink: api.Link =
    get match {
      case l: api.Link => l
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.LinkType, x))
    }

  @throws(classOf[ExtensionException])
  def getReporter: api.AnonymousReporter =
    get match {
      case t: api.AnonymousReporter => t
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.ReporterType, x))
    }

  @throws(classOf[ExtensionException])
  def getCommand: api.AnonymousCommand =
    get match {
      case t: api.AnonymousCommand => t
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.CommandType, x))
    }


  @throws(classOf[ExtensionException])
  def getCode: JList[Token] =
    get match {
      case t: JList[Token] @unchecked => t
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.CodeBlockType, x))
    }

  @throws(classOf[ExtensionException])
  def getSymbol: Token =
    get match {
      case t: Token => t
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.SymbolType, x))
    }


  /**
   * Used by the GIS extension. Other extensions should use
   * getSymbol where possible. The compiler turns the reference into a _const,
   * we just ensure the value is a reference.
   */
  @throws(classOf[ExtensionException])
  @throws(classOf[LogoException])
  def getReference: Reference =
    get match {
      case ref: Reference => ref
      case x =>
        throw new ExtensionException(
          getExceptionMessage(Syntax.ReferenceType, x))
    }

  /**
   * Used by the rnd extension. Other extensions should use getReporter where possible
   */
  def unevaluatedArgument: Reporter = arg

  // if you're looking for the cities extension's <code>getReporter</code>
  // method, that has been removed. Change the extension and use
  // <code>getSymbol</code> instead.

  def getExceptionMessage(wantedType: Int, badValue: AnyRef) =
    "Expected this input to be " +
      TypeNames.aName(wantedType) + " but got " +
       (if(badValue == Nobody) "NOBODY"
        else ("the " + api.TypeNames.name(badValue) + " " +
              Dump.logoObject(badValue)) +
              " instead.")

}
