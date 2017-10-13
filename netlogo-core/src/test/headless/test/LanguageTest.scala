// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless.test

import org.nlogo.core.AgentKind

case class LanguageTest(
  suiteName: String,
  testName: String,
  entries: List[Entry],
  modes: List[TestMode] = List(NormalMode, RunMode),
  versionLimit: VersionLimit = AnyVersion) {
  val fullName = s"$suiteName::$testName"
}

sealed trait Entry
case class Open(modelPath: String)                                    extends Entry
case class Declaration(source: String)                                extends Entry
case class Command(command: String,
  kind: AgentKind = AgentKind.Observer, result: Result = Success("")) extends Entry
case class Reporter(reporter: String, result: Result)                 extends Entry
case class Compile(result: Result)                                    extends Entry

sealed trait Result
case class Success       (message: String) extends Result
case class CompileError  (message: String) extends Result
case class RuntimeError  (message: String) extends Result
case class StackTrace    (message: String) extends Result

sealed abstract class TestMode
case object NormalMode extends TestMode
case object RunMode extends TestMode

sealed trait VersionLimit
case object AnyVersion extends VersionLimit
case object ThreeDOnly extends VersionLimit
case object TwoDOnly extends VersionLimit
case object HeadlessOnly extends VersionLimit
