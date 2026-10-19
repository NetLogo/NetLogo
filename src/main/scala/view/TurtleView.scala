package view25d.view

import com.jogamp.opengl.{ GLCapabilities, GLProfile }
import com.jogamp.opengl.awt.GLJPanel

import java.awt.{ BorderLayout, Color, Dimension }
import java.awt.event.{ WindowAdapter, WindowEvent }
import javax.swing.WindowConstants

import org.nlogo.api.{ AgentException, AgentSet, AnonymousReporter, LogoException, Color => NLColor, Context, Turtle }
import org.nlogo.app.App
import org.nlogo.theme.ThemeSync

import scala.jdk.CollectionConverters.IterableHasAsScala

import view25d.View25DExtension
import view25d.view.gl.TurtleGL

trait TurtleView extends Varview {

  protected val initialAgents: AgentSet

  protected var stemColorReporterOpt: Option[AnonymousReporter] = None
  protected var agents: AgentSet                                = initialAgents

  // Don't define here; `PatchViewGUI` with mix in an implementation from `VarviewWindow` --Jason B. (8/28/25)
  def dispose(): Unit

  def refresh(): Unit                       = ()
  def setStemThickness(value: Double): Unit = ()
  def updateTurtleShapes(): Unit            = ()

  def setAgentSet(as: AgentSet): Unit = {
    agents = as
  }

  def setStemColorReporter(reporter: AnonymousReporter): Unit = {
    stemColorReporterOpt = Option(reporter)
  }

}

class TurtleViewHeadless(override val title: String, override protected val initialAgents: AgentSet, reporter: AnonymousReporter)
  extends TurtleView {

  override def dispose(): Unit                                                                                = ()
  override def setupForRendering(wWidth: Int, wHeight: Int, minX: Int, maxX: Int, minY: Int, maxY: Int): Unit = ()
  override def setVisible(isVisible: Boolean): Unit                                                           = ()

}

class TurtleViewGUI(title: String, override protected val initialAgents: AgentSet, reporter: AnonymousReporter)
  extends VarviewWindow(title) with TurtleView with ThemeSync {

  private val glManager = new TurtleGL(this)

  def getGLWindow: MouseableGLWindow =
    glManager

  var turtleReporterValues = Array[TurtleValue]()
  var linkValues = Array[LinkValue]()

  var patchColorMatrix = Array[Array[Color]]()

  var viewOptions = new TurtleViewOptions(this, true, true, true, false, true, false)

  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)

  addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent): Unit = {
      View25DExtension.removeTurtleWindowWithTitle(title)
    }
  })

  private val glCanvas = new GLJPanel(new GLCapabilities(GLProfile.get(GLProfile.GL2)))

  glCanvas.addGLEventListener(glManager)
  glCanvas.addMouseListener(glManager)
  glCanvas.addMouseMotionListener(glManager)
  glCanvas.addMouseWheelListener(glManager)

  glManager.setCanvas(glCanvas)

  mainPanel.add(glCanvas, BorderLayout.CENTER)
  mainPanel.add(viewOptions, BorderLayout.NORTH)

  // change if re-add scalemanipulator
  setSize(new Dimension(600, 640))

  centerWindow(this)

  syncTheme()

  override def manuallyRefreshReporterView(context: Context): Unit = {
    updateArrayList(context)
    glManager.repaintCanvas()
  }

  override def updateTurtleShapes(): Unit = {
    glManager.updateTurtleDisplayList()
    glManager.repaintCanvas()
  }

  private def updateArrayList(context: Context): Unit = {
    turtleReporterValues = agents.agents.asScala.toArray.collect {
      case turtle: Turtle if !turtle.hidden =>
        TurtleValue(turtle.shape, NLColor.getColor(turtle.color), turtle.xcor, turtle.ycor, turtle.size,
                    reporter.report(context, Array(turtle)).asInstanceOf[Double], 0, getStemColor(context, turtle))
    }

    // Get Set of Links associated with the Turtles
    linkValues = getLinkSetFromTurtleSet(agents).toArray.map(link => {
      val end1 = link.end1
      val end2 = link.end2

      LinkValue(link.shape, NLColor.getColor(link.color), link.lineThickness, end1.xcor, end1.ycor,
                reporter.report(context, Array(end1)).asInstanceOf[Double], end2.xcor, end2.ycor,
                reporter.report(context, Array(end2)).asInstanceOf[Double])
    })

    if (viewOptions.usePColor)
      updatePColors()
  }

  private def updatePColors(): Unit = {
    for (i <- 0 until worldWidth){
      for (j <- 0 until worldHeight) {
        try {
          patchColorMatrix(i)(j) =
            NLColor.getColor(App.app.workspace.world.getPatchAt(i + minPxcor, j + minPycor).pcolor)
        }

        catch {
          case e: AgentException => e.printStackTrace()
        }
      }
    }
  }

  def setupForRendering(wWidth: Int, wHeight: Int, minX: Int, maxX: Int, minY: Int, maxY: Int): Unit = {
    worldWidth = wWidth
    worldHeight = wHeight
    minPxcor = minX
    minPycor = minY
    maxPxcor = maxX
    maxPycor = maxY

    turtleReporterValues = Array()
    linkValues = Array()

    patchColorMatrix = Array.fill(worldWidth) { Array.fill(worldHeight) { Color.BLUE } }
  }

  override def resetPerspective(): Unit = {
    glManager.observer.goHome(this)
    refresh()
  }

  override def refresh(): Unit = {
    glManager.repaintCanvas()

    if (viewOptions.usePColor)
      updatePColors()
  }

  override def zoomZby(change: Double): Unit = {
    if (-change < zScale)
      zScale += change

    glManager.repaintCanvas()
  }

  def trimStemColor(stemColor: Double): Double =
    stemColor.max(0).min(139.9)

  def getStemColor(context: Context, turtle: Turtle): Double = {
    stemColorReporterOpt.map {
      stemColorReporter =>
        try {
          trimStemColor(stemColorReporter.report(context, Array(turtle)).asInstanceOf[Double])
        } catch {
          case t: Throwable =>
            throw new LogoException("Stem color reporter did not generate a number. ", t) {}
        }
    }.getOrElse(4.5f)
  }

  override def syncTheme(): Unit = {
    dashboard.syncTheme()
    viewOptions.syncTheme()
  }
}
