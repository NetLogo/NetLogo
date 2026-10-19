package view25d.prims

import org.nlogo.api.{ AgentException, AnonymousReporter, Argument, Command, Context, ExtensionException,
                       ReporterRunnable }
import org.nlogo.app.App
import org.nlogo.core.Syntax

import view25d.View25DExtension
import view25d.view.{ PatchView, PatchViewGUI, PatchViewHeadless }

class MakePatchView extends Command {
  override def getSyntax: Syntax =
    Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.ReporterType), agentClassString = "O---")

  override def perform(args: Array[Argument], context: Context): Unit = {
    val title =  args(0).getString.trim

    if (title.isEmpty)
      throw new ExtensionException("Window title cannot be empty.\nThis is the identifier for your window")

    // test the patchReporter against patch 0,0 (throw away the result of report here)
    val patchReporter = args(1).getReporter

    try {
      patchReporter.report(context, Array(App.app.workspace.world.getPatchAt(0, 0)))
    }

    catch {
      case e: AgentException =>
        throw new ExtensionException(s"Error in accessing patches. $e")
      case t: Throwable =>
        throw new ExtensionException(s"Error in processing your reporter. $t")
    }

    context.workspace.updateUI()

    val newPatchView = context.workspace.waitForResult(new InitPatchView(context, title, patchReporter))

    if (newPatchView != null)
      View25DExtension.storePatchWindowWithTitle(title, newPatchView)
  }

  private class InitPatchView(context: Context, title: String, patchReporter: AnonymousReporter)
    extends ReporterRunnable[PatchView] {

    override def run(): PatchView = {
      try {

        val manualPatchView =
          if (context.workspace.workspaceContext.workspaceGUI)
            new PatchViewGUI(title, patchReporter)
          else
            new PatchViewHeadless(title, patchReporter)

        val world = context.world

        manualPatchView.setupForRendering(world.worldWidth, world.worldHeight, world.minPxcor, world.maxPxcor,
                                          world.minPycor, world.maxPycor)
        manualPatchView.manuallyRefreshReporterView(context)
        manualPatchView.setVisible(true)

        return manualPatchView
      }

      catch {
        case t: Throwable => t.printStackTrace()
      }

      return null
    }
  }
}
