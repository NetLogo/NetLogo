// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

import org.nlogo.internalapi.{
  CompiledModel, CompiledWidget, CompiledButton => ApiCompiledButton,
  EmptyRunnableModel, NonCompiledWidget, RunnableModel }
import org.nlogo.api.{ JobOwner, MersenneTwisterFast, NetLogoLegacyDialect }
import org.nlogo.agent.World
import org.nlogo.internalapi.{ AddProcedureRun, ModelAction, ModelUpdate,
  RunComponent, SchedulerWorkspace, StopProcedure, UpdateInterfaceGlobal }
import org.nlogo.core.{ AgentKind, Button => CoreButton, Chooser => CoreChooser,
  CompilerException, InputBox => CoreInputBox, Model, NumericInput, Program,
  Slider => CoreSlider, StringInput, Switch => CoreSwitch, Widget }
import org.nlogo.nvm.{ CompilerResults, Procedure, SuspendableJob }
import org.nlogo.workspace.{ AbstractWorkspace, Evaluating }


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

class CompiledRunnableModel(workspace: AbstractWorkspace with SchedulerWorkspace, compiledWidgets: Seq[CompiledWidget]) extends RunnableModel  {
  import workspace.scheduledJobThread

  val componentMap = Map.empty[String, RunComponent]

  override def submitAction(action: ModelAction): Unit = {
    scheduleAction(action, None)
  }

  override def submitAction(action: ModelAction, component: RunComponent): Unit = {
    scheduleAction(action, Some(component))
  }

  private var taggedComponents = Map.empty[String, RunComponent]

  private def registerTag(componentOpt: Option[RunComponent], action: ModelAction, tag: String): Unit = {
    componentOpt.foreach { component =>
      taggedComponents = taggedComponents + (tag -> component)
      component.tagAction(action, tag)
    }
  }

  private def scheduleAction(action: ModelAction, componentOpt: Option[RunComponent]): Unit = {
    action match {
      case UpdateInterfaceGlobal(name, value) =>
        val tag = scheduledJobThread.scheduleOperation( { () =>
            println(s"setting value of $name to ${value.get}")
            workspace.world.setObserverVariableByName(name, value.get)
        })
        registerTag(componentOpt, action, tag)
      case AddProcedureRun(widgetTag, isForever) =>
        // TODO: this doesn't take isForever into account yet
        val p = findWidgetProcedure(widgetTag)
        findWidgetProcedure(widgetTag).foreach { procedure =>
          val job =
            new SuspendableJob(workspace.world.observers, isForever, procedure, 0, null, workspace, workspace.world.mainRNG)
          val tag = scheduledJobThread.scheduleJob(job)
          registerTag(componentOpt, action, tag)
        }
      case StopProcedure(jobTag) => scheduledJobThread.stopJob(jobTag)
    }
  }

  def findWidgetProcedure(tag: String): Option[Procedure] = {
    compiledWidgets.collect {
      case c@CompiledButton(_, _, t, procedure) if t == tag && procedure != null => procedure
    }.headOption
  }

  def notifyUpdate(update: ModelUpdate): Unit = {
    taggedComponents.get(update.tag).foreach(_.updateReceived(update))
    taggedComponents -= update.tag
  }
}

case class CompiledButton(val widget: CoreButton, val compilerError: Option[CompilerException], val procedureTag: String, val procedure: Procedure)
  extends ApiCompiledButton

object CompileAll {
  def apply(model: Model, workspace: AbstractWorkspace with SchedulerWorkspace): CompiledModel = {
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
      val compiledWidgets = model.widgets.map(compileWidget(results, workspace))

      CompiledModel(model,
        compiledWidgets,
        new CompiledRunnableModel(workspace, compiledWidgets),
        Right(results.program))
    } catch {
      case e: CompilerException =>
        CompiledModel(model, Seq(), EmptyRunnableModel, Left(e))
      case e: Exception =>
        println("exception!")
        throw e
      case s: scala.NotImplementedError =>
        s.printStackTrace()
        throw s
    }
  }

  def compileWidget(results: CompilerResults, workspace: AbstractWorkspace)(widget: Widget): CompiledWidget = {
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
          val tag = s" __button-" + b.hashCode
          val source = s"to $tag [] $headerCode $repeatStart \n $buttonSource \n $repeatEnd end"
          val displayName = b.display.getOrElse(buttonSource.trim.replaceAll("\\s+", " "))

          try {
            val buttonResults =
              workspace.compiler.compileMoreCode(source, Some(displayName),
                results.program, results.proceduresMap,
                workspace.getExtensionManager, workspace.getCompilationEnvironment)
            buttonResults.head.init(workspace)
            CompiledButton(b, None, tag, buttonResults.head)
          } catch {
            case e: CompilerException =>
              CompiledButton(b, Some(e), "", null)
          }
        } getOrElse NonCompiledWidget(widget)

      case _ => NonCompiledWidget(widget)
    }
  }
}
