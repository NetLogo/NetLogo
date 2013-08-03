// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import org.nlogo.api
import LanguageTesting.{ TestMode, NormalMode, RunMode }

class Runner(t: LanguageTest) extends LanguageTesting {

  def run() {
    runOneMode(NormalMode)
    if(!t.testName.startsWith("*"))
      runOneMode(RunMode)
  }

  // use a custom owner so we get fullName into the stack traces
  // we get on the JobThread - ST 1/26/11
  override def owner =
    new api.SimpleJobOwner(t.fullName, workspace.world.mainRNG)

  def runOneMode(mode: TestMode) {
    try {
      init()
      val nonProcs = t.entries.filterNot(_.isInstanceOf[Procedure])
      defineProcedures(t.entries.collect{
        case p: Procedure => p.source}.mkString("\n"))
      nonProcs.foreach(runEntry(mode, _))
    }
    finally workspace.dispose()
  }

  def runEntry(mode: TestMode, entry: Entry) {
    entry match {
      case Open(modelPath) =>
        open(modelPath)
      case Procedure(content) =>
        defineProcedures(content)
      case Command(kind, command, Success(_)) =>
        testCommand(command, kind, mode)
      case Command(kind, command, RuntimeError(message)) =>
        testCommandError(command, message, kind, mode)
      case Command(kind, command, CompileError(message)) =>
        testCommandCompilerErrorMessage(command, message, kind)
      case Command(kind, command, StackTrace(message)) =>
        testCommandErrorStackTrace(command, message, kind, mode)
      case Reporter(reporter, Success(message)) =>
        testReporter(reporter, message, mode)
      case Reporter(reporter, RuntimeError(message)) =>
        testReporterError(reporter, message, mode)
      case Reporter(reporter, StackTrace(message)) =>
        testReporterErrorStackTrace(reporter, message, mode)
    }
  }

}
