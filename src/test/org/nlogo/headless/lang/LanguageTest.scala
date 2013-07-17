// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

case class LanguageTest(suiteName: String, testName: String, commands: List[String]) {
  val fullName = suiteName + "::" + testName
  val lineItems = commands.map(Parser.parse)
  val proc = Proc(lineItems.collect{
    case p: Proc => p.content}.mkString("\n"))
  val nonProcs = lineItems.filterNot(_.isInstanceOf[Proc])
}

// The output of the parser is lists of instances of these classes:
sealed trait Entry
case class OpenModel(modelPath: String) extends Entry
case class Proc(content: String) extends Entry
case class Command(agentKind: String, command: String) extends Entry
case class CommandWithError(agentKind: String, command: String, message: String) extends Entry
case class CommandWithStackTrace(agentKind: String, command: String, stackTrace: String) extends Entry
case class CommandWithCompilerError(agentKind: String, command: String, message: String) extends Entry
case class ReporterWithResult(reporter: String, result: String) extends Entry
case class ReporterWithError(reporter: String, error: String) extends Entry
case class ReporterWithStackTrace(reporter: String, stackTrace: String) extends Entry
