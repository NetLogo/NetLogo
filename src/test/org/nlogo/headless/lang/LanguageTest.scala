// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import org.nlogo.api.AgentKind

case class LanguageTest(suiteName: String, testName: String, entries: List[Entry]) {
  val fullName = suiteName + "::" + testName
}

sealed trait Entry
case class Open(modelPath: String)                                   extends Entry
case class Procedure(source: String)                                 extends Entry
case class Command(
  kind: AgentKind, command: String, result: Result = Success(""))    extends Entry
case class Reporter(reporter: String, result: Result)                extends Entry

sealed trait Result
case class Success       (message: String) extends Result
case class CompileError  (message: String) extends Result
case class RuntimeError  (message: String) extends Result
case class StackTrace    (message: String) extends Result
