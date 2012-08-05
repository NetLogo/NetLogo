// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.{ CompilerException, JobOwner, Program, Version }
import org.nlogo.nvm.{ CompilerResults, Procedure }
import org.nlogo.workspace.AbstractWorkspace
import collection.JavaConverters._
class CompilerManager(workspace: GUIWorkspace, proceduresInterface: ProceduresInterface)
extends Event.LinkChild
with Events.CompileMoreSourceEventHandler
with Events.InterfaceGlobalEventHandler
with Events.LoadBeginEventHandler
with Events.LoadEndEventHandler
with Events.WidgetAddedEventHandler
with Events.WidgetRemovedEventHandler
with Events.CompileAllEventHandler {

  val widgets = collection.mutable.Set[JobOwner]()
  val globalWidgets = collection.mutable.Set[InterfaceGlobalWidget]()

  override def getLinkParent = workspace

  private var isLoading = false

  override def handle(e: Events.LoadBeginEvent) {
    isLoading = true
    widgets.clear()
    globalWidgets.clear()
    // we can't clear all here because globals and such might not be allocated yet however, we're
    // about to change the program in world, which can be needed to clear the turtles. ev 1/17/07
    workspace.world.clearLinks()
    workspace.world.program(Program.empty)
    workspace.world.rememberOldProgram()
    workspace.world.turtleBreedShapes.setUpBreedShapes(
      true, workspace.world.program.breeds) // true = clear old
    workspace.world.linkBreedShapes.setUpBreedShapes(
      true, workspace.world.program.linkBreeds) // true = clear old
  }

  override def handle(e: Events.LoadEndEvent) {
    isLoading = false
    compileAll()
  }

  private def compileAll() {
    (new Events.RemoveAllJobsEvent).raise(this)
    workspace.world.displayOn(true)
    // We can't compile the Code tab until the contents of InterfaceGlobals is known, which won't
    // happen until the widgets are loaded, which happens later.  So the isLoading flag is used to
    // suppress compilation now.  Later, the handle() handler (which runs after everything has
    // loaded, including widgets) will take care of calling this method again. - ST 7/7/06
    if (!isLoading) {
      val proceed = compileProcedures()
      if (proceed) {
        workspace.world.realloc()
        workspace.world.rememberOldProgram()
        setGlobalVariables() // also updates constraints
        compileWidgets()
      } else {
        // even if compilation of the procedure tab fails, we still want to mark our constraints as
        // out of date, so that any existing dynamic constraints are thrown away since they're
        // compiled against the old program -- CLB
        updateInterfaceGlobalConstraints()
        resetWidgetProcedures()
      }
    }
  }

  private def compileProcedures(): Boolean = {
    workspace.world.program(Program.empty)
    try {
      val results =
          workspace.compiler.compileProgram(
            proceduresInterface.innerSource,
            workspace.world.program.copy(interfaceGlobals = globalWidgets.map(_.name).toSeq),
            workspace.getExtensionManager)
      workspace.procedures = results.proceduresMap
      for(procedure <- workspace.procedures.values)
        if (procedure.fileName.isEmpty)
          procedure.setOwner(proceduresInterface)
      workspace.init()
      workspace.world.program(results.program)
      new Events.CompiledEvent(
        proceduresInterface, results.program, null, null)
        .raise(this)
      true
    } catch { case error: CompilerException =>
      if (error.fileName.isEmpty)
        new Events.CompiledEvent(proceduresInterface, null, null, error)
          .raise(this)
      false
    }
  }

  private def setGlobalVariables() {
    for(w <- globalWidgets)
      workspace.world.setObserverVariableByName(w.name, w.valueObject)
  }

  private def updateInterfaceGlobalConstraints() {
    globalWidgets.foreach(_.updateConstraints())
  }

  private def compileSource(owner: JobOwner,
                            errorEvents: collection.mutable.Buffer[Events.CompiledEvent]) {
    try {
      val results =
        workspace.compiler.compileMoreCode(
          owner.source, Some(owner.classDisplayName + " '" + owner.displayName + "'"),
          workspace.world.program, workspace.procedures, workspace.getExtensionManager)
      if (!results.procedures.isEmpty) {
        results.head.init(workspace)
        results.head.setOwner(owner)
        new Events.CompiledEvent(
          owner, workspace.world.program, results.head, null).raise(this)
      }
    } catch { case error: CompilerException =>
      errorEvents +=
        new Events.CompiledEvent(
          owner, workspace.world.program, null, error)
    }
  }

  private def compileWidgets() {
    val errorEvents = collection.mutable.Buffer[Events.CompiledEvent]()
    val iter = widgets.iterator
    // handle special case where there are no more widgets.
    if (!iter.hasNext)
      new Events.CompiledEvent(
        null, workspace.world.program, null, null)
        .raise(this)
    while (iter.hasNext) {
      val owner = iter.next()
      if (!owner.isCommandCenter)
        compileSource(owner, errorEvents)
    }
    errorEvents.foreach(_.raise(this))
    // Ensure that newly compiled constraints are updated.
    updateInterfaceGlobalConstraints()
  }

  private def resetWidgetProcedures() {
    widgets.collect{case jw: JobWidget => jw}
      .foreach(_.procedure(null))
  }

  def handle(e: Events.CompileMoreSourceEvent) {
    if (isLoading)
      widgets.add(e.owner)
    else if (e.owner.isCommandCenter)
      try {
        val results =
          workspace.compiler.compileMoreCode(
            e.owner.source, Some(e.owner.classDisplayName), workspace.world.program,
            workspace.procedures, workspace.getExtensionManager)
        results.head.init(workspace)
        results.head.setOwner(e.owner)
        new Events.CompiledEvent(
          e.owner, workspace.world.program, results.head, null)
          .raise(this)
      }
      catch { case error: CompilerException =>
        new Events.CompiledEvent(
          e.owner, workspace.world.program, null, error)
          .raise(this)
      }
    else
      compileWidgets()
  }

  def handle(e: Events.InterfaceGlobalEvent) {
    import e.widget
    globalWidgets.add(widget)
    if (e.nameChanged)
      compileAll()
    // this check is needed because it might be a brand new widget
    // that doesn't have a variable yet - ST 3/3/04
    else if (workspace.world.observerOwnsIndexOf(widget.name.toUpperCase) != -1) {
      if (e.updating)
        widget.valueObject(
          workspace.world.getObserverVariableByName(widget.name))
      // note that we do this even if e.updating is true -- that's because
      // the widget may not have accepted the new value as is - ST 8/17/03
      try {
        val obj = widget.valueObject
        // Only set the global if the value has changed.  This prevents
        // use from firing our constraint code all the time.
        if (obj ne workspace.world.getObserverVariableByName(widget.name)) {
          // so that we do not interrupt without-interruption
          workspace.world.synchronized {
            workspace.world.setObserverVariableByName(widget.name, widget.valueObject)
          }
        }
      }
      catch {
        case ex: org.nlogo.api.ValueConstraint.Violation =>
          // If we have a Violation, then just ignore it because it appears the constraints have
          // changed on us since the widget had it's value set.  The widget will be updated and thus
          // will result in its current value being constrained appropriately by the widget. -- CLB
          org.nlogo.util.Exceptions.ignore(ex)
        case ex: org.nlogo.api.LogoException =>
          // A Logo exception here is ignored to avoid a never ending cascade of error popups.  Like
          // the ignoring of Violation exceptions, we are working under the assumption that Sliders
          // will always give us a coerced value back. -- CLB
          org.nlogo.util.Exceptions.ignore(ex)
      }
    }
  }

  def handle(e: Events.WidgetAddedEvent) {
    e.widget match {
      case owner: JobOwner => widgets.add(owner)
      case _ =>
    }
    e.widget match {
      case igw: InterfaceGlobalWidget => globalWidgets.add(igw)
      case _ =>
    }
  }

  def handle(e: Events.CompileAllEvent) {
    compileAll()
  }

  def handle(e: Events.WidgetRemovedEvent) {
    e.widget match {
      case owner: JobOwner => widgets.remove(owner)
      case _ =>
    }
    e.widget match {
      case igw: InterfaceGlobalWidget => globalWidgets.remove(igw)
      case _ =>
    }
  }

}
