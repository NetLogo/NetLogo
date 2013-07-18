// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import org.nlogo.api
import LanguageTesting.{ TestMode, NormalMode, RunMode }

object Runner {
  // run the test in both modes, Normal and Run
  def apply(t: LanguageTest) {
    def agentKind(a: String) = a match {
      case "O" => api.AgentKind.Observer
      case "T" => api.AgentKind.Turtle
      case "P" => api.AgentKind.Patch
      case "L" => api.AgentKind.Link
      case x => sys.error("unrecognized agent type: " + x)
    }
    class Tester(mode: TestMode) extends LanguageTesting {
      // use a custom owner so we get fullName into the stack traces
      // we get on the JobThread - ST 1/26/11
      override def owner =
        new api.SimpleJobOwner(t.fullName, workspace.world.mainRNG)
      try {
        init()
        defineProcedures(t.proc.content)
        t.nonProcs.foreach {
          case OpenModel(modelPath) =>
            workspace.open(modelPath)
          case Proc(content) =>
            defineProcedures(content)
          case Command(agent, command) =>
            testCommand(command, agentKind(agent), mode)
          case CommandWithError(agent, command, message) =>
            testCommandError(command, message, agentKind(agent), mode)
          case CommandWithCompilerError(agent, command, message) =>
            testCommandCompilerErrorMessage(command, message, agentKind(agent))
          case CommandWithStackTrace(agent, command, stackTrace) =>
            testCommandErrorStackTrace(command, stackTrace, agentKind(agent), mode)
          case ReporterWithResult(reporter, result) =>
            testReporter(reporter, result, mode)
          case ReporterWithError(reporter, error) =>
            testReporterError(reporter, error, mode)
          case ReporterWithStackTrace(reporter, error) =>
            testReporterErrorStackTrace(reporter, error, mode)
        }
      }
      finally workspace.dispose()
    }
    new Tester(NormalMode)
    if(!t.testName.startsWith("*"))
      new Tester(RunMode)
  }
}
