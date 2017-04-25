// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.javafx

import org.nlogo.internalapi.{
  CompiledModel, CompiledWidget, CompiledButton => ApiCompiledButton,
  CompiledMonitor => ApiCompiledMonitor,
  EmptyRunnableModel, NonCompiledWidget }
import org.nlogo.api.{ JobOwner, MersenneTwisterFast, NetLogoLegacyDialect }
import org.nlogo.agent.World
import org.nlogo.internalapi.{ ModelUpdate, Monitorable, MonitorsUpdate, SchedulerWorkspace }
import org.nlogo.core.{ AgentKind, Button => CoreButton, Chooser => CoreChooser,
  CompilerException, InputBox => CoreInputBox, Model, Monitor => CoreMonitor, NumericInput, NumberParser, Program,
  Slider => CoreSlider, StringInput, Switch => CoreSwitch, Widget }
import org.nlogo.nvm.{ CompilerResults, ConcurrentJob, ExclusiveJob, Procedure, SuspendableJob }
import org.nlogo.workspace.{ AbstractWorkspace, Evaluating }

import scala.util.Try

class DummyJobOwner(val random: MersenneTwisterFast, val tag: String) extends JobOwner {
  def displayName: String = "Job Owner" // TODO: we may want another button
  def isButton: Boolean = true // TODO: our only owners at this point are buttons
  def isCommandCenter: Boolean = false
  def isLinkForeverButton: Boolean = false
  def isTurtleForeverButton: Boolean = false
  def ownsPrimaryJobs: Boolean = true

  def classDisplayName: String = "Button"
  def headerSource: String = ""
  def innerSource: String = ""
  def innerSource_=(s: String): Unit = {}
  def kind: org.nlogo.core.AgentKind = AgentKind.Observer
  def source: String = ""
}

object CompileAll {
  def apply(model: Model, workspace: AbstractWorkspace with SchedulerWorkspace): CompiledModel = {
    val widgetActions = new WidgetActions(workspace, workspace.scheduledJobThread)
    //TODO: We're forcing this to be a 2D Program
    val program = Program.fromDialect(NetLogoLegacyDialect).copy(interfaceGlobals = model.interfaceGlobals)
    try {
      val results =
        workspace.compiler.compileProgram(model.code, Seq(), program,
          workspace.getExtensionManager,
          workspace.getCompilationEnvironment)
      workspace.setProcedures(results.proceduresMap)
      workspace.init()
      workspace.world.asInstanceOf[org.nlogo.agent.CompilationManagement].program(results.program)
      val compiledWidgets = model.widgets.map(compileWidget(results, workspace, widgetActions))

      CompiledModel(model,
        compiledWidgets,
        widgetActions,
        Right(results.program))
    } catch {
      case e: CompilerException =>
        CompiledModel(model, Seq(), EmptyRunnableModel, Left(e))
      case e: Exception =>
        println("exception!" + e.getMessage)
        e.printStackTrace()
        throw e
      case s: scala.NotImplementedError =>
        s.printStackTrace()
        throw s
    }
  }

  def compileWidget(results: CompilerResults, workspace: AbstractWorkspace, widgetActions: WidgetActions)(widget: Widget): CompiledWidget = {
    def compileCode(source: String, name: String): Try[Procedure] = {
      Try {
        val compilerResults =
          workspace.compiler.compileMoreCode(source, Some(name),
            results.program, results.proceduresMap,
            workspace.getExtensionManager, workspace.getCompilationEnvironment)
          val proc = compilerResults.head
          proc.init(workspace)
          proc
      }
    }

    widget match {
      case b: CoreButton =>
        b.source map { buttonSource =>
          val headerCode = b.buttonKind match {
            case AgentKind.Observer => "__observercode"
            case AgentKind.Turtle => "__turtlecode"
            case AgentKind.Patch => "__patchcode"
            case AgentKind.Link => "__linkcode"
          }
          val (repeatStart, repeatEnd) = ("", "__done")
          val tag = s"__button-${b.hashCode}"
          val source = s"to $tag [] $headerCode $repeatStart \n $buttonSource \n $repeatEnd end"
          val displayName = b.display.getOrElse(buttonSource.trim.replaceAll("\\s+", " "))

          compileCode(source, displayName).fold(
            {
              case e: CompilerException => CompiledButton(b, Some(e), "", null, widgetActions)
              case other => throw other
            },
            proc => CompiledButton(b, None, tag, proc, widgetActions))
        } getOrElse NonCompiledWidget(widget)
      case m: CoreMonitor =>
        m.source map { monitorSource =>
          val tag = s"__monitor-${m.hashCode}"
          val source = s"to-report $tag [] __observercode \n report __monitorprecision (\n ${monitorSource} \n) ${m.precision} end"
          val displayName = m.display.orElse(m.source).getOrElse("")

          compileCode(source, displayName).fold({
            case e: CompilerException => CompiledMonitor(m, Some(e), "", null, source, widgetActions)
            case other => throw other
          },
          proc => CompiledMonitor(m, None, tag, proc, source, widgetActions))
        } getOrElse NonCompiledWidget(widget)
      case s: CoreSlider =>
        def decorateSource(body: String, name: String): String = {
          // I'm not sure that __done is needed here
          s"to-report $name [] __observercode report (\n$body\n) __done end"
        }
        def makeCompiledMonitorable(monitorType: String, default: Double, source: String): CompiledMonitorable[Double] = {
          val name = s"__slider-${s.hashCode}-${monitorType}"
          val procSource = decorateSource(source, name)
          compileCode(procSource, name).fold({
            case e: CompilerException =>
              CompiledMonitorable[Double](default, Some(e), "", null, procSource)
            case other => throw other
          },
          proc => CompiledMonitorable[Double](default, None, name, proc, procSource))
        }
        def monitorable(monitorType: String, default: Double, source: String): Monitorable[Double] =
          NumberParser.parse(source) match {
            case Right(jdouble) => NonCompiledMonitorable[Double](jdouble.doubleValue)
            case Left(_) => makeCompiledMonitorable(monitorType, default, source)
          }


        // we have to make the reporter for the variable
        val value = makeCompiledMonitorable("value", s.default, s.variable.getOrElse(s.default.toString))
        // for the rest, we need to check whether they are numbers or not
        // If they are numbers, we're done and we make them into NonCompiledMonitorables
        val min = monitorable("min", 0.0,   s.min)
        val max = monitorable("max", 100.0, s.max)
        val inc = monitorable("inc", 1.0,   s.step)
        CompiledSlider(s, value, min, max, inc, widgetActions)
      case _ => NonCompiledWidget(widget)
    }
  }
}
