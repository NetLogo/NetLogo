// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.core.CompilerException
import org.nlogo.api.{ JobOwner, SourceOwner }
import org.nlogo.api.AgentException
import org.nlogo.core.Program
import org.nlogo.nvm.{ CompilerResults, Procedure }
import org.nlogo.workspace.AbstractWorkspace
import org.nlogo.window.Event.LinkChild
import org.nlogo.window.Events.{
  CompileMoreSourceEvent, CompiledEvent, InterfaceGlobalEvent,
  LoadBeginEvent, LoadEndEvent, RemoveAllJobsEvent,
  WidgetAddedEvent, WidgetRemovedEvent, CompileAllEvent }
import org.nlogo.api.AgentException
import org.nlogo.api.LogoException
import org.nlogo.api.ValueConstraint
import org.nlogo.api.Exceptions
import org.nlogo.api.LogoException

import scala.collection.mutable.HashSet
import scala.collection.JavaConversions._

class CompilerManager(val workspace: AbstractWorkspace,
  val proceduresInterface: ProceduresInterface,
  eventRaiser: (Event, Object) => Unit = (e:Event, o:Object) => e.raise(o))
    extends LinkChild
    with CompileMoreSourceEvent.Handler
    with InterfaceGlobalEvent.Handler
    with LoadBeginEvent.Handler
    with LoadEndEvent.Handler
    with WidgetAddedEvent.Handler
    with WidgetRemovedEvent.Handler
    with CompileAllEvent.Handler {

  private[window] val widgets       = HashSet[JobOwner]()
  private[window] val globalWidgets = HashSet[InterfaceGlobalWidget]()

  private def raiseEvent(e: Event): Unit =
    eventRaiser(e, this)

  private var isLoading: Boolean = false

  def getLinkParent: Object = workspace

  ///

  def handle(e: LoadBeginEvent): Unit = {
    isLoading = true
    widgets.clear()
    globalWidgets.clear()
    // we can't clear all here because globals and such might not be allocated yet
    // however, we're about to change the program in world, which can be needed to
    // clear the turtles. ev 1/17/07
    workspace.world.clearLinks()
    // not really clear on why the rigmarole here with two different
    // Program objects is necessary, but when I tried using the same
    // one, I got ClassCastExceptions when I tried to open the
    // Capacitance model - ST 12/5/07
    workspace.world.program(workspace.world.newProgram)
    workspace.world.rememberOldProgram()
    val program = workspace.world.newProgram
    workspace.world.program(program)
    workspace.world.setUpShapes(true)
  }

  def handle(e: LoadEndEvent): Unit = {
    isLoading = false
    compileAll()
  }

  def handle(e: CompileMoreSourceEvent): Unit = {
    val owner = e.owner
    if (isLoading)
      widgets += owner
    else if (! owner.isCommandCenter)
      compileWidgets()
    else {
      try {
        val displayName = Some.apply(owner.classDisplayName)
        val results =
          workspace.compiler.compileMoreCode(owner.source,
            displayName, workspace.world.program,
            workspace.getProcedures, workspace.getExtensionManager,
            workspace.getCompilationEnvironment);
        results.head.init(workspace)
        results.head.owner = owner
        raiseEvent(new CompiledEvent(owner, workspace.world.program, results.head, null))
      } catch {
        case error: CompilerException =>
          raiseEvent(new CompiledEvent(owner, workspace.world.program, null, error))
      }
    }
  }

  def handle(e: InterfaceGlobalEvent): Unit = {
    val widget = e.widget
    globalWidgets += e.widget
    if (e.nameChanged)
      compileAll()
    // this check is needed because it might be a brand new widget
    // that doesn't have a variable yet - ST 3/3/04
    else if (workspace.world.observerOwnsIndexOf(widget.name.toUpperCase) != -1) {
      if (e.updating) {
        widget.valueObject(workspace.world.getObserverVariableByName(widget.name))
      }
      // note that we do this even if e.updating() is true -- that's because
      // the widget may not have accepted the new value as is - ST 8/17/03
      try {
        val widgetValue = widget.valueObject

        // Only set the global if the value has changed.  This prevents
        // us from firing our constraint code all the time.
        if (widgetValue ne workspace.world.getObserverVariableByName(widget.name)) {
          // so that we do not interrupt without-interruption
          workspace.world.synchronized {
            workspace.world.setObserverVariableByName(widget.name, widgetValue)
          }
        }
      } catch {
        case ex: ValueConstraint.Violation =>
          // If we have a Violation, then just ignore it because it
          // appears the constraints have changed on us since the
          // widget had it's value set.  The widget will be updated
          // and thus will result in its current value being constrained
          // appropriately by the widget. -- CLB
          Exceptions.ignore(ex)
        case ex: AgentException => throw new IllegalStateException(ex)
        case ex: LogoException =>
          // A Logo exception here is ignored to avoid a never
          // ending cascade of error popups.  Like the ignoring
          // of Violation exceptions, we are working under the
          // assumption that Sliders will always give us a
          // coerced value back. -- CLB
          Exceptions.ignore(ex)
      }
    }
  }

  def handle(e: CompileAllEvent): Unit = {
    compileAll()
  }

  def handle(e: WidgetAddedEvent): Unit = {
    e.widget match {
      case jobOwner: JobOwner => widgets += jobOwner
      case _ =>
    }
    e.widget match {
      case interfaceGlobal: InterfaceGlobalWidget => globalWidgets += interfaceGlobal
      case _ =>
    }
  }

  def handle(e: WidgetRemovedEvent): Unit = {
    e.widget match {
      case jobOwner: JobOwner => widgets -= jobOwner
      case _ =>
    }
    e.widget match {
      case interfaceGlobal: InterfaceGlobalWidget => globalWidgets -= interfaceGlobal
      case _ =>
    }
  }

  private def compileAll(): Unit = {
    raiseEvent(new RemoveAllJobsEvent())
    workspace.world.displayOn(true)
    // We can't compile the Code tab until the contents of
    // InterfaceGlobals is known, which won't happen until the
    // widgets are loaded, which happens later.  So the isLoading
    // flag is used to suppress compilation now.  Later,
    // the handle() handler (which runs after everything
    // has loaded, including widgets) will take care of calling
    // this method again. - ST 7/7/06
    if (!isLoading) {
      var proceed = compileProcedures()
      if (proceed) {
        workspace.world.realloc()
        workspace.world.rememberOldProgram()
        setGlobalVariables() // also updates constraints
        compileWidgets()
      } else {
        // even if compilation of the procedure tab fails, we still want to mark our
        // constraints as out of date, so that any existing dynamic constraints are
        // thrown away since they're compiled against the old program -- CLB
        updateInterfaceGlobalConstraints()
        resetWidgetProcedures()
      }
    }
  }

  private def compileProcedures(): Boolean = {
    val program = workspace.world.newProgram(getGlobalVariableNames)
    workspace.world.program(program)
    try {
      val owners =
        if (workspace.aggregateManager != null)
          Seq[SourceOwner](workspace.aggregateManager)
        else
          Seq()

      val results =
        workspace.compiler.compileProgram(
          proceduresInterface.innerSource, owners, program,
          workspace.getExtensionManager, workspace.getCompilationEnvironment)
      workspace.setProcedures(results.proceduresMap)
      workspace.getProcedures.values.foreach { procedure =>
        val owner = procedure.filename match {
          case ""          => proceduresInterface
          case "aggregate" => workspace.aggregateManager
          case fileName    => new ExternalFileInterface(fileName)
        }
        procedure.owner = owner
      }
      workspace.init()
      workspace.world.program(results.program)
      raiseEvent(new CompiledEvent(proceduresInterface, results.program, null, null))
      true
    } catch {
      case error: CompilerException =>
        val errorSource = error.filename match {
          case ""          => proceduresInterface
          case "aggregate" => workspace.aggregateManager
          case fileName    => new ExternalFileInterface(fileName)
        }
        raiseEvent(new CompiledEvent(errorSource, null, null, error))
        false
    }
  }

  private def setGlobalVariables(): Unit = {
    globalWidgets.foreach { widget =>
      try {
        workspace.world.setObserverVariableByName(widget.name, widget.valueObject)
      } catch {
        case ex@(_: AgentException | _: LogoException) =>
          throw new IllegalStateException(ex)
      }
    }
  }

  private def updateInterfaceGlobalConstraints(): Unit = {
    globalWidgets.foreach(_.updateConstraints())
  }

  // this returns an error event, if n error was encountered
  private def compileSource(owner: JobOwner): Option[CompiledEvent] = {
    try {
      val displayName =
        Some(s"${owner.classDisplayName} '${owner.displayName}'")
      val results: CompilerResults =
        workspace.compiler.compileMoreCode(owner.source, displayName,
          workspace.world.program, workspace.getProcedures,
          workspace.getExtensionManager, workspace.getCompilationEnvironment)

      if (!results.procedures.isEmpty) {
        results.head.init(workspace)
        results.head.owner = owner
        raiseEvent(new CompiledEvent(owner, workspace.world.program, results.head, null))
      }
      None
    } catch {
      case error: CompilerException =>
        Some(new CompiledEvent(owner, workspace.world.program, null, error))
    }
  }

  private def compileWidgets(): Unit = {
    // handle special case where there are no more widgets.
    if (widgets.isEmpty) {
      raiseEvent(new CompiledEvent(null, workspace.world.program, null, null))
    } else {
      val errorEvents = widgets.foldLeft(Seq[CompiledEvent]()) {
        case (errors, widget) if ! widget.isCommandCenter =>
          compileSource(widget).map(e => errors :+ e).getOrElse(errors)
        case (errors, widget) => errors
      }
      errorEvents.foreach(raiseEvent)
    }

    // Ensure that newly compiled constraints are updated.
    updateInterfaceGlobalConstraints()
  }

  private def resetWidgetProcedures(): Unit = {
    widgets.foreach {
      case jw: JobWidget => jw.procedure = null
      case _ =>
    }
  }

  private def getGlobalVariableNames: Seq[String] =
    globalWidgets.map(_.name).toSeq
}
