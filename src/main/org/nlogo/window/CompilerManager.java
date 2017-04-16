// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.api.CompilerException;
import org.nlogo.api.JobOwner;
import org.nlogo.api.Program;
import org.nlogo.nvm.CompilerResults;
import org.nlogo.nvm.Procedure;
import org.nlogo.workspace.AbstractWorkspace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public strictfp class CompilerManager
    implements org.nlogo.window.Event.LinkChild,
    org.nlogo.window.Events.CompileMoreSourceEvent.Handler,
    org.nlogo.window.Events.InterfaceGlobalEvent.Handler,
    org.nlogo.window.Events.LoadBeginEvent.Handler,
    org.nlogo.window.Events.LoadEndEvent.Handler,
    org.nlogo.window.Events.WidgetAddedEvent.Handler,
    org.nlogo.window.Events.WidgetRemovedEvent.Handler,
    org.nlogo.window.Events.CompileAllEvent.Handler {

  private final AbstractWorkspace workspace;
  private final ProceduresInterface proceduresInterface;

  public final Set<JobOwner> widgets =
      new HashSet<JobOwner>();
  public final Set<InterfaceGlobalWidget> globalWidgets =
      new HashSet<InterfaceGlobalWidget>();

  public CompilerManager(AbstractWorkspace workspace, ProceduresInterface proceduresInterface) {
    this.workspace = workspace;
    this.proceduresInterface = proceduresInterface;
  }

  public Object getLinkParent() {
    return workspace;
  }

  ///

  private boolean isLoading = false;

  public void handle(org.nlogo.window.Events.LoadBeginEvent e) {
    isLoading = true;
    widgets.clear();
    globalWidgets.clear();
    // we can't clear all here because globals and such might not be allocated yet
    // however, we're about to change the program in world, which can be needed to
    // clear the turtles. ev 1/17/07
    workspace.world.clearLinks();
    // not really clear on why the rigmarole here with two different
    // Program objects is necessary, but when I tried using the same
    // one, I got ClassCastExceptions when I tried to open the
    // Capacitance model - ST 12/5/07
    workspace.world.program(workspace.world.newProgram());
    workspace.world.rememberOldProgram();
    Program program = workspace.world.newProgram();
    workspace.world.program(program);
    workspace.world.turtleBreedShapes.setUpBreedShapes(true, program.breeds()); // true = clear old
    workspace.world.linkBreedShapes.setUpBreedShapes(true, program.linkBreeds()); // true = clear old
  }

  public void handle(org.nlogo.window.Events.LoadEndEvent e) {
    isLoading = false;
    compileAll();
  }

  ///

  private void compileAll() {
    new org.nlogo.window.Events.RemoveAllJobsEvent().raise(this);
    workspace.world.displayOn(true);
    // We can't compile the Code tab until the contents of
    // InterfaceGlobals is known, which won't happen until the
    // widgets are loaded, which happens later.  So the isLoading
    // flag is used to suppress compilation now.  Later,
    // the handle() handler (which runs after everything
    // has loaded, including widgets) will take care of calling
    // this method again. - ST 7/7/06
    if (!isLoading) {
      boolean proceed = compileProcedures();
      if (proceed) {
        workspace.world.realloc();
        workspace.world.rememberOldProgram();
        setGlobalVariables(); // also updates constraints
        compileWidgets();

      } else {
        // even if compilation of the procedure tab fails, we still want to mark our
        // constraints as out of date, so that any existing dynamic constraints are
        // thrown away since they're compiled against the old program -- CLB
        updateInterfaceGlobalConstraints();
        resetWidgetProcedures();
      }
    }
  }

  private boolean compileProcedures() {
    workspace.world.program(workspace.world.newProgram());
    try {
      CompilerResults results =
          workspace.compiler().compileProgram
              (proceduresInterface.innerSource(), workspace.world.newProgram(getGlobalVariableNames()),
                  workspace.getExtensionManager());
      workspace.setProcedures(results.proceduresMap());
      for (Procedure procedure : workspace.getProcedures().values()) {
        if (procedure.fileName.equals("")) {
          procedure.setOwner(proceduresInterface);
        } else if (procedure.fileName.equals("aggregate")) {
          procedure.setOwner(workspace.aggregateManager());
        } else {
          procedure.setOwner(new ExternalFileInterface(procedure.fileName));
        }
      }
      workspace.init();
      workspace.world.program(results.program());
      new org.nlogo.window.Events.CompiledEvent
          (proceduresInterface, results.program(), null, null)
          .raise(this);
      return true;
    } catch (CompilerException error) {
      if (AbstractWorkspace.isApplet()) {
        System.err.println("CompilerException: " + error);
        error.printStackTrace();
        new org.nlogo.window.Events.CompiledEvent
            (proceduresInterface, null, null, error)
            .raise(this);
      }
      if (error.fileName().equals("")) {
        new org.nlogo.window.Events.CompiledEvent
            (proceduresInterface, null, null, error)
            .raise(this);
      } else if (error.fileName().equals("aggregate")) {
        new org.nlogo.window.Events.CompiledEvent
            (workspace.aggregateManager(), null, null, error)
            .raise(this);
      } else {
        new org.nlogo.window.Events.CompiledEvent
            (new ExternalFileInterface(error.fileName()), null, null, error)
            .raise(this);
      }
      return false;
    }
  }

  private void setGlobalVariables() {
    Iterator<InterfaceGlobalWidget> iter = globalWidgets.iterator();
    while (iter.hasNext()) {
      InterfaceGlobalWidget w = iter.next();
      try {
        workspace.world.setObserverVariableByName(w.name(), w.valueObject());
      } catch (org.nlogo.api.AgentException ex) {
        throw new IllegalStateException(ex);
      } catch (org.nlogo.api.LogoException ex) {
        throw new IllegalStateException(ex);
      }
    }
  }

  private void updateInterfaceGlobalConstraints() {
    for (InterfaceGlobalWidget w : globalWidgets) {
      w.updateConstraints();
    }
  }


  private void compileSource(org.nlogo.api.JobOwner owner,
                             List<org.nlogo.window.Events.CompiledEvent> errorEvents) {
    try {
      CompilerResults results =
          workspace.compiler().compileMoreCode
              (owner.source(), scala.Some.apply(owner.classDisplayName() + " '" + owner.displayName() + "'"),
                  workspace.world.program(), workspace.getProcedures(), workspace.getExtensionManager());

      if (!results.procedures().isEmpty()) {
        results.head().init(workspace);
        results.head().setOwner(owner);
        new org.nlogo.window.Events.CompiledEvent
            (owner, workspace.world.program(), results.head(), null).raise(this);
      }
    } catch (CompilerException error) {
      errorEvents.add
          (new org.nlogo.window.Events.CompiledEvent
              (owner, workspace.world.program(), null, error));
    }
  }

  private void compileWidgets() {
    List<org.nlogo.window.Events.CompiledEvent> errorEvents =
        new ArrayList<org.nlogo.window.Events.CompiledEvent>();
    Iterator<JobOwner> iter = widgets.iterator();
    // handle special case where there are no more widgets.
    if (!iter.hasNext()) {
      new org.nlogo.window.Events.CompiledEvent
          (null, workspace.world.program(), null, null)
          .raise(this);
    }
    while (iter.hasNext()) {
      JobOwner owner = iter.next();
      if (!owner.isCommandCenter()) {
        compileSource(owner, errorEvents);
      }
    }
    for (org.nlogo.window.Events.CompiledEvent event : errorEvents) {
      event.raise(this);
    }

    // Ensure that newly compiled constraints are updated.
    updateInterfaceGlobalConstraints();
  }

  private void resetWidgetProcedures() {
    for (JobOwner owner : widgets) {
      if (owner instanceof JobWidget) {
        ((JobWidget) owner).procedure(null);
      }
    }
  }

  public void handle(org.nlogo.window.Events.CompileMoreSourceEvent e) {
    JobOwner owner = e.owner;
    if (isLoading) {
      widgets.add(owner);
      return;
    }
    if (owner.isCommandCenter()) {
      try {
        CompilerResults results =
            workspace.compiler().compileMoreCode
                (owner.source(), scala.Some.apply(owner.classDisplayName()), workspace.world.program(),
                    workspace.getProcedures(), workspace.getExtensionManager());
        results.head().init(workspace);
        results.head().setOwner(owner);
        new org.nlogo.window.Events.CompiledEvent
            (owner, workspace.world.program(), results.head(), null).raise(this);
      } catch (CompilerException error) {
        new org.nlogo.window.Events.CompiledEvent
            (owner, workspace.world.program(), null, error)
            .raise(this);
      }
    } else {
      compileWidgets();
    }
  }

  public void handle(org.nlogo.window.Events.InterfaceGlobalEvent e) {
    InterfaceGlobalWidget widget = e.widget;
    globalWidgets.add(e.widget);
    if (e.nameChanged) {
      compileAll();
    }
    // this check is needed because it might be a brand new widget
    // that doesn't have a variable yet - ST 3/3/04
    else if (workspace.world.observerOwnsIndexOf(widget.name().toUpperCase()) != -1) {
      if (e.updating) {
        widget.valueObject
            (workspace.world.getObserverVariableByName(widget.name()));
      }
      // note that we do this even if e.updating() is true -- that's because
      // the widget may not have accepted the new value as is - ST 8/17/03
      try {
        Object val = widget.valueObject();

        // Only set the global if the value has changed.  This prevents
        // use from firing our constraint code all the time.
        if (val != workspace.world.getObserverVariableByName(widget.name())) {
          // so that we do not interrupt without-interruption
          synchronized (workspace.world) {
            workspace.world.setObserverVariableByName(widget.name(), widget.valueObject());
          }
        }
      } catch (org.nlogo.api.ValueConstraint.Violation ex) {
        // If we have a Violation, then just ignore it because it
        // appears the constraints have changed on us since the
        // widget had it's value set.  The widget will be updated
        // and thus will result in its current value being constrained
        // appropriately by the widget. -- CLB
        org.nlogo.util.Exceptions.ignore(ex);
      } catch (org.nlogo.api.AgentException ex) {
        throw new IllegalStateException(ex);
      } catch (org.nlogo.api.LogoException ex) {
        // A Logo exception here is ignored to avoid a never
        // ending cascade of error popups.  Like the ignoring
        // of Violation exceptions, we are working under the
        // assumption that Sliders will always give us a
        // coerced value back. -- CLB
        org.nlogo.util.Exceptions.ignore(ex);
      }
    }
  }

  public void handle(org.nlogo.window.Events.WidgetAddedEvent e) {
    Widget w = e.widget;
    if (w instanceof JobOwner) {
      widgets.add((JobOwner) w);
    }
    if (w instanceof InterfaceGlobalWidget) {
      globalWidgets.add((InterfaceGlobalWidget) w);
    }
  }

  public void handle(org.nlogo.window.Events.CompileAllEvent e) {
    compileAll();
  }

  public void handle(org.nlogo.window.Events.WidgetRemovedEvent e) {
    widgets.remove(e.widget);
    globalWidgets.remove(e.widget);
  }

  private List<String> getGlobalVariableNames() {
    List<String> result = new ArrayList<String>();
    for (InterfaceGlobalWidget w : globalWidgets) {
      result.add(w.name());
    }
    return result;
  }

}
