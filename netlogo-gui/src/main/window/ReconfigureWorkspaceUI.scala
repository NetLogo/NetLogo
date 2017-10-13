// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Container
import java.net.URI
import java.nio.file.{ Files, Paths }

import org.nlogo.window.Events.{ AfterLoadEvent, BeforeLoadEvent,
  LoadBeginEvent, LoadEndEvent, LoadModelEvent, LoadWidgetsEvent }
import org.nlogo.api.{ CompilerServices, ModelType }
import org.nlogo.core.Model

import scala.util.Try


/**
 * A brief overview on the UI-centric model load process:
 * (This information does not apply to Headless loading,
 *  which is a totally different animal)
 *
 * ReconfigureWorkspaceUI emits 6 events to get the Workspace and UI to open
 * a new model. These events handle the disposal of an old model as well as the
 * loading of a new model. Note that while the events themselves are entirely ordered
 * (BeforeLoadEvent and all its sub-actions always happen before LoadBeginEvent
 * and any of its sub-actions) the sub-actions within an event have *no guaranteed order*.
 * It probably wouldn't take six events to load a model if ordering guarantees could be
 * maintained between different events which depend on one another. After listing all
 * events and subactions, I will speculate a bit on ordering, but if you're thinking of
 * changing things here, please do your own research and testing because the description
 * I'll give is highly likely to be both incomplete and incorrect.
 *
 * 1. BeforeLoadEvent
 *   * Causes the workspace to clear / reset the jobManager, extensionManager, modelPath,
 *     compiled code, plots, ticks, etc.
 *   * Clears the drawing and mouse coordinates (this is handled by GUIWorkspace)
 *   * Causes the DirtyManager to reset itself
 *   * Sends out the "modelOpened" event from `NetLogoListenerManager`
 *   * Disposes of the SDM editor
 *   * Resets the recent files menu
 * 2. LoadBeginEvent
 *   * Clears all widgets from the widget panel
 *   * Hides the view (note that this *doesn't* hide the view *widget* which is visible throughout the loading process)
 *   * Clears all cached widgets and the cached program from the CompilerManager,
 *     as well as deleting all links.
 *   * Closes and clears the LabManager
 *   * Resets the interface tab, command center, speed slider, tick counter
 *   * Closes all ".nls" tabs, switches to the interface tab
 *   * Removes all widgets
 *   * closes all agent monitor windows
 *   * Sets the title / error dialog title
 *     (requires the modelPath to have been set in BeforeLoadEvent)
 * 3. LoadModelEvent
 *   * Updates ModelSaver, ModelTracker with the new model
 *   * Sets the source of the Info Tab, LabManager, AggregateManager
 *   * Sets the source of the CodeTab, which appears to trigger a full compilation
 *     (but actually doesn't, because widgets aren't yet loaded)
 *   * Workspace loads shapes into world, modelSettings, preview comamands, hubnet client
 * 4. LoadWidgetsEvent
 *   * Loads each widget in turn (relevant widgets use events to register themselves with CompilerManager)
 *   * The view widget is totally reconfigured, so `createPatches` is called on world
 *     (which requires clearing turtles and links, so that's done as well)
 * 5. LoadEndEvent
 *   * triggers full compilation (since widgets are now loaded). In addition to compiling the code, this:
 *     * runs realloc() on the world
 *     * runs clearAll() on the world
 *   * changes renderer topology (not sure why this isn't done as part of view loading in step 4) and makes the view visible
 *   * `App` performs the following miscellaneous functions:
 *     * resets the turtleShapesManager, linkShapesManager
 *     * asks the view to repaint
 *     * revalidates the interface panel
 *     * runs "smartPack" (AKA make the model the right size)
 *     * pulls the frame to the front and focuses the interface tab
 *   * The speedSlider and updateMode controls load their values from the workspace
 *     (set by goofy view-loading code in 4)
 * 6. AfterLoadEvent
 *   * DirtyMonitor sets itself up to track the new model
 *   * Tabs requests focus (this coming shortly after the App has told tabs.interfaceTab to requestFocus
 *   * Widgets update their constraints
 *   * PlotWidgets compile their plots
 *   * `GUIWorkspace` takes the following actions:
 *     * resetting observer perspective
 *     * resetting `UpdateManager`
 *     * setting up the default glView state (depending on whether we've just loaded a 2D or 3D model)
 *     * evaluating the command "startup"
 *   * The tick counter redraws itself
 *
 * Musings on dependency orderings:
 *
 * For all of its complications, there are basically three primary concerns whose order is significant when
 * loading a model. These concerns are maintenance of world state, compilation, and the widgets
 * (including, and especially, the view). Everything else is details that (while important) typically don't
 * determine the success or failure of the load.
 *
 * The dependencies between world state, program / compilation, and widgets are actually fairly complex. A quick browse
 * through the code of CompilerManager can provide a better idea of certain details of this process.
 * The table below presents a summarized view of changes that happen in each domain at each event timestep:
 *
 *            \|   world         | program / compilation         |   widgets           |
 * ------------+-----------------|-------------------------------+---------------------|
 * BeforeLoad  |                 | workspace clears procedures   |                     |
 * LoadBegin   | linksCleared    | * world.program cleared       | widgets cleared     |
 *             |                 | * code sources cleared        |                     |
 * LoadModel   |                 | sets code sources             |                     |
 * LoadWidgets | * clear old     | registers interface globals   | adds widgets        |
 *             | * createPatches |                               |                     |
 * LoadEnd     | * realloc       | full compilation              | changes render topo |
 * LoadEnd     | * clearAll      |                               |                     |
 * AfterLoad   | startup run     |                               | update constraints  |
 *
 */

object ReconfigureWorkspaceUI {
  def apply(linkParent: Container, uri: URI, modelType: ModelType, model: Model,
    compilerServices: CompilerServices): Unit = {
      new Loader(linkParent).loadHelper(uri, modelType, model, compilerServices)
  }

  private case class Loader(linkParent: Container) extends org.nlogo.window.Event.LinkChild {
    def getLinkParent = linkParent

    def loadHelper(modelURI: URI, modelType: ModelType, model: Model, compilerServices: CompilerServices) {
      val uriOption = Try(Paths.get(modelURI)).toOption
        .filterNot(p => p.getFileName.toString.startsWith("empty.nlogo"))
        .filter(p => Files.isRegularFile(p))
        .map(_.toString)
      val beforeEvents = List(
        new BeforeLoadEvent(uriOption, modelType),
        new LoadBeginEvent())

      val loadSectionEvents = List(
        new LoadModelEvent(model),
        new LoadWidgetsEvent(model.widgets))

      val afterEvents = List(new LoadEndEvent(), new AfterLoadEvent())
      // fire missles! (actually, just fire the events...)
      for (e <- beforeEvents ::: loadSectionEvents ::: afterEvents) e.raise(this)
    }
  }
}
