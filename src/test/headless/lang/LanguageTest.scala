// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import org.nlogo.api.AgentKind

case class LanguageTest(suiteName: String, testName: String, entries: List[Entry]) {
  val fullName = s"$suiteName::$testName"
  val modes =
    if(testName.startsWith("*"))
      List(NormalMode)
    else
      List(NormalMode, RunMode)
}

sealed trait Entry
case class Open(modelPath: String)                                    extends Entry
case class Declaration(source: String)                                extends Entry
case class Command(command: String,
  kind: AgentKind = AgentKind.Observer, result: Result = Success("")) extends Entry
case class Reporter(reporter: String, result: Result)                 extends Entry

sealed trait Result
case class Success       (message: String) extends Result
case class CompileError  (message: String) extends Result
case class RuntimeError  (message: String) extends Result
case class StackTrace    (message: String) extends Result

sealed abstract class TestMode
case object NormalMode extends TestMode
case object RunMode extends TestMode
