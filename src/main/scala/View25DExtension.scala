package view25d

import java.awt.GraphicsEnvironment
import java.lang.Double

import org.nlogo.api.{ Argument, Command, Context, DefaultClassManager, ExtensionManager,
                       PrimitiveManager, Reporter }
import org.nlogo.app.App
import org.nlogo.core.Syntax
import org.nlogo.workspace.AbstractWorkspace

import view25d.prims.{ DecoratePatchView, GetObserverAngles, GetObserverDistance, GetObserverFocus, GetZScale,
                       MakePatchView, MakeTurtleView, SetLinkDisplayMode, SetObserverAngles, SetObserverDistance,
                       SetObserverFocus, SetTurtleStemColor, SetTurtleStemThickness, SetZScale, UpdateAllViews,
                       UpdateOnePatchView, UpdateOneTurtleView }
import view25d.view.{ PatchView, TurtleView, View25DShapeChangeListener }

import scala.collection.mutable.Map

object View25DExtension {
  var isHeadless = true

  var patchWindowMap = Map[String, PatchView]()
  var turtleWindowMap = Map[String, TurtleView]()

  def numWindows: Int =
    patchWindowMap.size + turtleWindowMap.size

  // Add a window to the indexed set. Get rid of any existing window with that name
  def storePatchWindowWithTitle(title: String, window: PatchView): Unit = {
    removePatchWindowWithTitle(title)
    patchWindowMap += ((title, window))
  }

  // remove a patch window with given name, if any exists
  def removePatchWindowWithTitle(title: String): Unit = {
    patchWindowMap.get(title).foreach(window => {
      patchWindowMap -= title
      window.dispose()
    })
  }

  // Add a window to the indexed set. Remove any existing turtle view.
  def storeTurtleWindowWithTitle(title: String, window: TurtleView): Unit = {
    removeTurtleWindowWithTitle(title)
    turtleWindowMap += ((title, window))
  }

  // remove a turtle window with given name, if any exists
  def removeTurtleWindowWithTitle(title: String): Unit = {
    turtleWindowMap.get(title).foreach(window => {
      turtleWindowMap -= title
      window.dispose()
    })
  }

  // When a turtle shape is deleted or added notify all views
  def updateTurtleShapesAllViews(): Unit = {
    turtleWindowMap.values.foreach(_.updateTurtleShapes())
    patchWindowMap.values.foreach(_.updateTurtleShapes())
  }
}

class View25DExtension extends DefaultClassManager {
  import View25DExtension._

  private var syncFunctionIDOpt: Option[Long] = None

  // run at extension startup.  ensure that the NetLogo native JOGL is loaded.
  override def runOnce(manager: ExtensionManager): Unit = {
    View25DExtension.isHeadless = !manager.workspaceContext.workspaceGUI

    View25DShapeChangeListener.listen()

    if (!View25DExtension.isHeadless) {
      syncFunctionIDOpt =
        Option(
          App.app.addSyncFunction(
            () => {
              patchWindowMap.values.foreach(_.syncTheme())
              turtleWindowMap.values.foreach(_.syncTheme())
            }
          )
        )
    }

  }

  override def load(manager: PrimitiveManager): Unit = {
    manager.addPrimitive("patch-view", new MakePatchView())
    manager.addPrimitive("decorate-patch-view", new DecoratePatchView(true))
    manager.addPrimitive("undecorate-patch-view", new DecoratePatchView(false))

    manager.addPrimitive("turtle-view", new MakeTurtleView())

    manager.addPrimitive("update-patch-view", new UpdateOnePatchView())
    manager.addPrimitive("update-all-patch-views", new UpdateAllViews())

    manager.addPrimitive("update-turtle-view", new UpdateOneTurtleView())

    manager.addPrimitive("remove-patch-view", new RemoveOnePatchView())
    manager.addPrimitive("remove-turtle-view", new RemoveOneTurtleView())
    manager.addPrimitive("remove-all-patch-views", new RemoveAllPatchViews())
    manager.addPrimitive("remove-all-turtle-views", new RemoveAllTurtleViews())

    manager.addPrimitive("count-windows", new GetWindowCount())

    manager.addPrimitive("set-z-scale", new SetZScale())
    manager.addPrimitive("get-z-scale", new GetZScale())

    manager.addPrimitive("set-turtle-stem-thickness", new SetTurtleStemThickness())
    manager.addPrimitive("set-turtle-stem-color", new SetTurtleStemColor())

    manager.addPrimitive("get-observer-angles", new GetObserverAngles())
    manager.addPrimitive("set-observer-angles", new SetObserverAngles())

    manager.addPrimitive("get-observer-distance", new GetObserverDistance())
    manager.addPrimitive("set-observer-distance", new SetObserverDistance())
    manager.addPrimitive("get-observer-xy-focus", new GetObserverFocus())
    manager.addPrimitive("set-observer-xy-focus", new SetObserverFocus())

    manager.addPrimitive("show-links-xy-plane", new SetLinkDisplayMode(true))
    manager.addPrimitive("show-links-xyz", new SetLinkDisplayMode(false))
  }

  /// utility (or less user-facing) primitives

  private class GetWindowCount extends Reporter {
    override def getSyntax: Syntax =
      Syntax.reporterSyntax(ret = Syntax.NumberType)

    override def report(args: Array[Argument], context: Context): Double =
      numWindows.toDouble
  }

  private class RemoveOnePatchView extends Command {
    override def getSyntax: Syntax =
      Syntax.commandSyntax(right = List(Syntax.StringType));

    override def perform(args: Array[Argument], context: Context): Unit = {
      removePatchWindowWithTitle(args(0).getString)
    }
  }

  private class RemoveOneTurtleView extends Command {
    override def getSyntax: Syntax =
      Syntax.commandSyntax(right = List(Syntax.StringType))

    override def perform(args: Array[Argument], context: Context): Unit = {
      removeTurtleWindowWithTitle(args(0).getString)
    }
  }

  private class RemoveAllTurtleViews extends Command {
    override def getSyntax: Syntax =
      Syntax.commandSyntax()

    override def perform(args: Array[Argument], context: Context): Unit = {
      turtleWindowMap.values.foreach(_.dispose())
      turtleWindowMap.clear()
    }
  }

  private class RemoveAllPatchViews extends Command {
    override def getSyntax: Syntax =
      Syntax.commandSyntax()

    override def perform(args: Array[Argument], context: Context): Unit = {
      patchWindowMap.values.foreach(_.dispose())
      patchWindowMap.clear()
    }
  }

  /// end utility primitives

  private def disposeAllPatchViews(): Unit = {
    patchWindowMap.values.foreach(window => {
      if (window != null)
        window.dispose()
    })
  }

  private def disposeAllTurtleViews(): Unit = {
    turtleWindowMap.values.foreach(window => {
      if (window != null)
        window.dispose()
    })
  }

  // ensure that the windows are cleaned up when we unload the extension
  override def unload(manager: ExtensionManager): Unit = {
    disposeAllPatchViews()
    disposeAllTurtleViews()

    if (!View25DExtension.isHeadless) {
      syncFunctionIDOpt.foreach(App.app.removeSyncFunction)
      syncFunctionIDOpt = None
    }

  }

}
