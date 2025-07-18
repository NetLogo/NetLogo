// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import scala.util.parsing.input.CharArrayReader

object ProjectCombinators {
  def parse(input: String): Option[Project]  = {
    val reader = new CharArrayReader(input.toCharArray)
    val combinators = new ProjectCombinators

    combinators.declarations(reader) match {
      case combinators.Success(declarations, next) if next.atEnd =>
        for {
          name <- declarations.collectFirst{case Declaration.Name(x) => x}
          version <- declarations.collectFirst{case Declaration.Version(x) => x}
          dependencies = declarations.collectFirst{case Declaration.Dependencies(x) => x}.getOrElse(Seq.empty)
          sourceFiles = declarations.collectFirst{case Declaration.SourceFiles(x) => x}.getOrElse(Seq.empty)
        } yield Project(name, version, dependencies, sourceFiles)
      case _ =>
        None
    }
  }
}

case class VersionNumber(components: Seq[Int]) extends Ordered[VersionNumber] {
  assert(components.forall(_ >= 0))

  def compare(that: VersionNumber) = {
    components.zipAll(that.components, 0, 0).map{case (x, y) => x.compare(y)}.find(_ != 0).getOrElse(0)
  }
}

enum VersionBound:
  case Inclusive(version: VersionNumber)
  case Exclusive(version: VersionNumber)

enum Declaration:
  case Name(value: String)
  case Dependencies(value: Seq[Dependency])
  case Version(value: VersionNumber)
  case SourceFiles(value: Seq[String])

case class Dependency(name: String, minVersion: Option[VersionBound], maxVersion: Option[VersionBound])
case class Project(name: String, version: VersionNumber, dependencies: Seq[Dependency], sourceFiles: Seq[String])

class ProjectCombinators
extends scala.util.parsing.combinator.JavaTokenParsers {

  def string: Parser[String] =
    literal("\"") ~> "[^\"]*".r <~ literal("\"")

  def versionNumber: Parser[VersionNumber] =
    rep1sep(regex("[0-9]+".r), literal(".")) ^^ (x => VersionNumber(x.map(_.toInt)))

  def lowerVersionBound: Parser[VersionBound] =
    literal(">") ~> versionNumber ^^ VersionBound.Exclusive.apply |
    literal(">=") ~> versionNumber ^^ VersionBound.Inclusive.apply

  def upperVersionBound: Parser[VersionBound] =
    literal("<") ~> versionNumber ^^ VersionBound.Exclusive.apply |
    literal("<=") ~> versionNumber ^^ VersionBound.Inclusive.apply

  def exactVersionBound: Parser[VersionBound] =
    literal("==") ~> versionNumber ^^ VersionBound.Inclusive.apply

  def dependency: Parser[Dependency] =
    string ~ lowerVersionBound ~ literal("&&") ~! upperVersionBound ^^ {
      case name ~ lowerBound ~ _ ~ upperBound => Dependency(name, Some(lowerBound), Some(upperBound))
    } |
    string ~ lowerVersionBound ^^ {case name ~ bound => Dependency(name, Some(bound), None)} |
    string ~ upperVersionBound ^^ {case name ~ bound => Dependency(name, None, Some(bound))} |
    string ~ exactVersionBound ^^ {case name ~ bound => Dependency(name, Some(bound), Some(bound))} |
    string ^^ (name => Dependency(name, None, None))

  def nameDeclaration: Parser[Declaration] =
    literal("name") ~>! literal("=") ~>! string ^^ Declaration.Name.apply

  def versionDeclaration: Parser[Declaration] =
    literal("version") ~>! literal("=") ~>! versionNumber ^^ Declaration.Version.apply

  def dependenciesDeclaration: Parser[Declaration] =
    literal("dependencies") ~>! literal("=") ~>! rep1sep(dependency, literal(",")) ^^ Declaration.Dependencies.apply

  def sourceFilesDeclaration: Parser[Declaration] =
    literal("source-files") ~>! literal("=") ~>! rep1sep(string, literal(",")) ^^ Declaration.SourceFiles.apply

  def declarations: Parser[Seq[Declaration]] =
    rep(nameDeclaration | versionDeclaration | dependenciesDeclaration | sourceFilesDeclaration) <~! regex("\\s*".r)

}
