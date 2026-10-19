package view25d.prims

import org.nlogo.api.{ AgentSet, AnonymousReporter, Argument, Command, Context, ExtensionException,
                       ReporterRunnable, Turtle }
import org.nlogo.app.App
import org.nlogo.core.Syntax

import scala.jdk.CollectionConverters.IterableHasAsScala

import view25d.View25DExtension
import view25d.view.{ TurtleView, TurtleViewGUI, TurtleViewHeadless }

class MakeTurtleView extends Command {
  override def getSyntax: Syntax =
    Syntax.commandSyntax(right = List(Syntax.StringType, Syntax.AgentsetType, Syntax.ReporterType),
                         agentClassString = "O---")

  override def perform(args: Array[Argument], context: Context): Unit = {
    val title =  args(0).getString.trim

    if (title.isEmpty)
      throw new ExtensionException("Window title cannot be empty.\nThis is the identifier for your window")

    //test the reporter against the supplied turtles (throw away the result of report here)
    val as = args(1).getAgentSet
    val turtleReporter = args(2).getReporter

    try {
      as.agents.asScala.foreach(agent => turtleReporter.report(context, Array(agent.asInstanceOf[Turtle])))
    }

    catch {
      case t: Throwable =>
        throw new ExtensionException(s"Error in processing your reporter. $t")
    }

    context.workspace.updateUI()

    val newTurtleView = context.workspace.waitForResult(new InitTurtleView(context, as, title, turtleReporter))

    View25DExtension.storeTurtleWindowWithTitle(title, newTurtleView)
  }

  private class InitTurtleView(context: Context, as: AgentSet, title: String, turtleReporter: AnonymousReporter)
    extends ReporterRunnable[TurtleView] {

    override def run(): TurtleView = {

      val manualTurtleView =
        if (context.workspace.workspaceContext.workspaceGUI)
          new TurtleViewGUI(title, as, turtleReporter)
        else
          new TurtleViewHeadless(title, as, turtleReporter)

      val world = context.world

      manualTurtleView.setupForRendering(world.worldWidth, world.worldHeight, world.minPxcor, world.maxPxcor,
                                         world.minPycor, world.maxPycor)
      manualTurtleView.manuallyRefreshReporterView(context)
      manualTurtleView.setVisible(true)

      manualTurtleView
    }
  }
}
