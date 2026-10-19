package view25d.view

import com.jogamp.opengl.{ GLCapabilities, GLProfile }
import com.jogamp.opengl.awt.GLJPanel

import java.awt.{ BorderLayout, Dimension }
import java.awt.event.{ WindowAdapter, WindowEvent }
import javax.swing.WindowConstants

import org.nlogo.agent.Turtle
import org.nlogo.api.{ AgentException, AnonymousReporter, Color, Context }
import org.nlogo.app.App
import org.nlogo.theme.ThemeSync

import scala.jdk.CollectionConverters.IterableHasAsScala

import view25d.View25DExtension
import view25d.view.gl.PatchGL

trait PatchView extends Varview {

  var doingTurtles = false

  // Don't define here; `PatchViewGUI` with mix in an implementation from `VarviewWindow` --Jason B. (8/28/25)
  def dispose(): Unit

  def updateTurtleShapes(): Unit = ()

}

class PatchViewHeadless(override val title: String, reporter: AnonymousReporter) extends PatchView with Varview {
  override def dispose(): Unit                                                                                = ()
  override def setupForRendering(wWidth: Int, wHeight: Int, minX: Int, maxX: Int, minY: Int, maxY: Int): Unit = ()
  override def setVisible(isVisible: Boolean): Unit                                                           = ()
}

class PatchViewGUI(title: String, reporter: AnonymousReporter) extends VarviewWindow(title) with PatchView with ThemeSync {
  private val glManager = new PatchGL(this)

  def getGLWindow: MouseableGLWindow =
    glManager

  var viewOptions = new PatchViewOptions(this, true, false, false, true, false)

  var reporterValueMatrix = Array[Array[PatchValue]]()

  var turtleValues = Array[TurtleValue]()
  var linkValues = Array[LinkValue]()

  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)

  // window listener to ensure that the Map gets cleaned up.
  addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent): Unit = {
      View25DExtension.removePatchWindowWithTitle(title)
    }
  })

  val glCanvas =  new GLJPanel(new GLCapabilities(GLProfile.get(GLProfile.GL2)))

  glCanvas.addGLEventListener(glManager);
  glCanvas.addMouseListener(glManager);
  glCanvas.addMouseMotionListener(glManager);
  glCanvas.addMouseWheelListener(glManager);

  glManager.setCanvas(glCanvas)

  mainPanel.add(glCanvas, BorderLayout.CENTER)
  mainPanel.add(viewOptions, BorderLayout.NORTH)

  setSize(new Dimension(600, 650))

  centerWindow(this)

  syncTheme()

  def setupForRendering(wWidth: Int, wHeight: Int, minX: Int, maxX: Int, minY: Int, maxY: Int): Unit = {
    worldWidth = wWidth
    worldHeight = wHeight
    minPxcor = minX
    minPycor = minY
    maxPxcor = maxX
    maxPycor = maxY

    reporterValueMatrix = Array.fill(worldWidth) { new Array[PatchValue](worldHeight) }
  }

  def updateValueArray(context: Context): Unit = {
    for (i <- 0 until worldWidth){
      for (j <- 0 until worldHeight) {
        try {
          val patch = App.app.workspace.world.getPatchAt(i + minPxcor, j + minPycor)
          val c = Color.getColor(patch.pcolor)

          reporterValueMatrix(i)(j) = new PatchValue(c, reporter.report(context, Array(patch)).asInstanceOf[Double])
        }

        catch {
          case e: AgentException => e.printStackTrace()
        }
      }
    }

    if (doingTurtles) {
      val as = App.app.workspace.world.turtles

      turtleValues = as.agents.asScala.toArray.collect {
        case turtle: Turtle if !turtle.hidden =>
          // stemColor will be ignored, no stem in PatchView
          TurtleValue(turtle.shape, Color.getColor(turtle.color), turtle.xcor, turtle.ycor, turtle.size,
                      reporter.report(context, Array(turtle)).asInstanceOf[Double], turtle.heading, 4.5f)
      }

      // Get Set of Links associated with the Turtles
      linkValues = getLinkSetFromTurtleSet(as).toArray.map(link => {
        val end1 = link.end1
        val end2 = link.end2

        LinkValue(link.shape, Color.getColor(link.color), link.lineThickness, end1.xcor, end1.ycor,
                  reporter.report(context, Array(end1)).asInstanceOf[Double], end2.xcor, end2.ycor,
                  reporter.report(context, Array(end2)).asInstanceOf[Double])
      })
    }

    else {
      turtleValues = Array()
      linkValues = Array()
    }
  }

  override def manuallyRefreshReporterView(context: Context): Unit = {
    updateValueArray(context)
    glManager.repaintCanvas()
  }

  def refresh(): Unit = {
    glManager.repaintCanvas()
  }

  override def updateTurtleShapes(): Unit = {
    glManager.updateTurtleDisplayList()
    glManager.repaintCanvas()
  }

  override def resetPerspective(): Unit = {
    glManager.observer.goHome(this)
    glManager.repaintCanvas()
  }

  override def zoomZby(change: Double): Unit = {
    if (-change < zScale)
      zScale += change

    glManager.repaintCanvas()
  }

  override def syncTheme(): Unit = {
    dashboard.syncTheme()
    viewOptions.syncTheme()
  }

}
