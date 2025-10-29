// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.util.Locale

import org.nlogo.core.{ AgentVariableSet, Command, Dialect, Femto, Instruction, Reporter, Resource, Syntax,
                        TokenMapperInterface }

import scala.collection.immutable.ListMap

object NetLogoLegacyDialect extends Dialect {
  override val is3D           = false
  override val agentVariables = new AgentVariableSet {
    val implicitObserverVariableTypeMap: ListMap[String, Int] = ListMap()
    val implicitTurtleVariableTypeMap:   ListMap[String, Int] = AgentVariables.implicitTurtleVariableTypeMap(false)
    val implicitPatchVariableTypeMap:    ListMap[String, Int] = AgentVariables.implicitPatchVariableTypeMap(false)
    val implicitLinkVariableTypeMap:     ListMap[String, Int] = AgentVariables.implicitLinkVariableTypeMap
  }
  override val tokenMapper    = NetLogoLegacyDialectTokenMapper

  def isAvailable = getClass.getResource(NetLogoLegacyDialectTokenMapper.path) != null

  case class _magicopen(name: Option[String]) extends Command {
    def syntax = Syntax.commandSyntax(
      agentClassString = "O---",
      right = name.map(_ => List()).getOrElse(List(Syntax.StringType)))
  }
}

import NetLogoLegacyDialect._magicopen

trait DelegatingMapper extends TokenMapperInterface {
  def defaultMapper: TokenMapperInterface
  def path:    String
  def pkgName: String

  private def entries(entryType: String): Iterator[(String, String)] =
    for {
      line <- Resource.lines(path)
      if !line.startsWith("#")
      split = line.split(" ")
      if split(0) == entryType
    } yield split(1).toUpperCase(Locale.ENGLISH) -> (s"$pkgName.${split(2)}")

  lazy val commands  = entries("C").toMap
  lazy val reporters = entries("R").toMap

  def allCommandNames: Set[String]  = (commands.keySet ++ defaultMapper.allCommandNames)
  def allReporterNames: Set[String] = (reporters.keySet ++ defaultMapper.allReporterNames)

  private def instantiate[T](name: String) =
    Class.forName(name).getDeclaredConstructor().newInstance().asInstanceOf[T]

  private def magicOpenToken(s: String): Option[Command] =
    if (s.startsWith("___") && s.length > 3)
      Some(_magicopen(Some(s.stripPrefix("___"))))
    else if (s.equalsIgnoreCase("_magic-open"))
      Some(_magicopen(None))
    else
      None

  def overrideBreedInstruction(primName: String, breedName: String): Option[Instruction] = None

  def getCommand(s: String): Option[Command] =
    commands.get(s.toUpperCase(Locale.ENGLISH)).map(instantiate[Command]) orElse
      magicOpenToken(s) orElse defaultMapper.getCommand(s)

  def getReporter(s: String): Option[Reporter] =
    reporters.get(s.toUpperCase(Locale.ENGLISH)).map(instantiate[Reporter]) orElse
      defaultMapper.getReporter(s)

  def breedInstruction(primName: String, breedName: String): Option[Instruction] =
    overrideBreedInstruction(primName, breedName) orElse
      defaultMapper.breedInstruction(primName, breedName)
}

object NetLogoLegacyDialectTokenMapper extends DelegatingMapper {
  val defaultMapper = Femto.get[TokenMapperInterface]("org.nlogo.parse.TokenMapper")
  val path = "/system/tokens-legacy.txt"
  val pkgName = "org.nlogo.compile.prim"
}
