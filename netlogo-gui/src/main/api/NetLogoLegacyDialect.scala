// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ AgentVariableSet, DefaultTokenMapper, Dialect, NetLogoCore, Resource,
  TokenMapperInterface => CoreTokenMapperInterface, Command => CoreCommand, Reporter => CoreReporter, Syntax => CoreSyntax }

object NetLogoLegacyDialect extends Dialect {
  override val is3D           = false
  override val agentVariables = new AgentVariableSet {
    val getImplicitObserverVariables: Seq[String] = Seq()
    val getImplicitTurtleVariables: Seq[String]   = AgentVariables.getImplicitTurtleVariables(false)
    val getImplicitPatchVariables: Seq[String]    = AgentVariables.getImplicitPatchVariables(false)
    val getImplicitLinkVariables: Seq[String]     = AgentVariables.getImplicitLinkVariables
  }
  override val tokenMapper    = NetLogoLegacyDialectTokenMapper

  case class _magicopen(name: Option[String]) extends CoreCommand {
    def syntax = CoreSyntax.commandSyntax(
      agentClassString = "O---",
      right = name.map(_ => List()).getOrElse(List(CoreSyntax.StringType)))
  }
}

import NetLogoLegacyDialect._magicopen

trait DelegatingMapper extends CoreTokenMapperInterface {
  def defaultMapper: CoreTokenMapperInterface
  def path:    String
  def pkgName: String

  private def entries(entryType: String): Iterator[(String, String)] =
    for {
      line <- Resource.lines(path)
      if !line.startsWith("#")
      Array(tpe, primName, className) = line.split(" ")
      if tpe == entryType
    } yield primName.toUpperCase -> (s"$pkgName.$className")

  lazy val commands  = entries("C").toMap
  lazy val reporters = entries("R").toMap

  def allCommandNames: Set[String]  = (commands.keySet ++ defaultMapper.allCommandNames)
  def allReporterNames: Set[String] = (reporters.keySet ++ defaultMapper.allReporterNames)

  private def instantiate[T](name: String) =
    Class.forName(name).newInstance.asInstanceOf[T]

  private def magicOpenToken(s: String): Option[CoreCommand] =
    if (s.startsWith("___") && s.length > 3)
      Some(_magicopen(Some(s.stripPrefix("___"))))
    else if (s.equalsIgnoreCase("_magic-open"))
      Some(_magicopen(None))
    else
      None

  def getCommand(s: String): Option[CoreCommand] =
    commands.get(s.toUpperCase).map(instantiate[CoreCommand]) orElse
      magicOpenToken(s) orElse defaultMapper.getCommand(s)

  def getReporter(s: String): Option[CoreReporter] =
    reporters.get(s.toUpperCase).map(instantiate[CoreReporter]) orElse
      defaultMapper.getReporter(s)
}

object NetLogoLegacyDialectTokenMapper extends DelegatingMapper {
  val defaultMapper = DefaultTokenMapper
  val path = "/system/tokens-legacy.txt"
  val pkgName = "org.nlogo.compiler.prim"
}
