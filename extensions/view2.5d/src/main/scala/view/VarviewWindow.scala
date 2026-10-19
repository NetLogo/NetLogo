package view25d.view

import java.awt.{ BorderLayout, Component, Dimension, Toolkit }

import javax.swing.{ JFrame, JPanel }

import org.nlogo.agent.Turtle
import org.nlogo.api.{ AgentSet, Context, Link }
import org.nlogo.app.App
import org.nlogo.swing.NetLogoIcon

import scala.jdk.CollectionConverters.IterableHasAsScala

import view25d.View25DExtension

object VarviewWindow {
  val ORBIT = 0
  val MOVE = 1
  val ZOOM = 2
  val RESET = -1
  val SCALEZ = 3
}

trait Varview {

  // scale factor for the rendered turtle/patch variable.
  var zScale = 1.0

  def title: String

  def setupForRendering(wWidth: Int, wHeight: Int, minX: Int, maxX: Int, minY: Int, maxY: Int): Unit
  def setVisible(isVisible: Boolean): Unit

  def manuallyRefreshReporterView(context: Context): Unit = ()
  def resetPerspective(): Unit                            = ()
  def setLinksDisplayMode(linksInXYPlane: Boolean): Unit  = ()
  def syncTheme(): Unit                                   = ()
  def zoomZby(num: Double): Unit                          = ()

  def getObserverDistance: Double                                                     = 0
  def getObserverPerspectiveAngles: Array[Double]                                     = Array(0, 0)
  def getObserverPerspectiveFocusPoint: Array[Double]                                 = Array(0, 0)
  def shiftObserverFocusPoint(deltax: Double, deltay: Double): Unit                   = ()
  def updateObserverPerspectiveAnglesWithDeltas(thetax: Double, thetay: Double): Unit = ()
  def zoomToDistance(dist: Double): Unit                                              = ()

}

abstract class VarviewWindow(override val title: String) extends JFrame(title) with Varview with NetLogoIcon {
  var worldWidth = 0
  var worldHeight = 0
  var viewWidth = 0
  var viewHeight = 0
  var minPxcor = 0
  var minPycor = 0
  var maxPxcor = 0
  var maxPycor = 0

  // pixels that add to the square-inner panel. used in asserting aspect ratio.
  var xExpand = 0
  var yExpand = 0

  def getGLWindow: MouseableGLWindow

  private var mode = VarviewWindow.ORBIT

  def setMode(newMode: Int): Unit = {
    mode = newMode
  }

  def getMode: Int =
    mode

  protected val dashboard = new VarviewDashboard(VarviewWindow.this)

  protected val mainPanel = new JPanel(new BorderLayout) {
    add(dashboard, BorderLayout.SOUTH)
  }

  getContentPane.add(mainPanel)

  protected def centerWindow(frame: Component): Unit = {
    val screenSize = Toolkit.getDefaultToolkit.getScreenSize
    val frameSize = new Dimension(frame.getWidth.min(screenSize.width), frame.getHeight.min(screenSize.height))

    val wincycle = 5

    val shiftWEach = (screenSize.width - frameSize.width) / wincycle
    val shiftHEach = (screenSize.height - frameSize.height) / wincycle

    val numWins = View25DExtension.numWindows

    val placeW = (numWins % wincycle) * shiftWEach + shiftWEach / 2
    val placeH = (numWins % wincycle) * shiftHEach + shiftHEach / 2

    frame.setLocation(placeW, placeH)
  }

  // Given an AgentSet of turtles, produce a Set of associated Links.
  // Assumes check that AgentSet contains Turtles has already been made.
  // Currently includes links for which only one end is in the AgentSet
  protected def getLinkSetFromTurtleSet(turtleSet: AgentSet): Set[Link] = {
    // no work to do if there are no links or no turtles
    if (App.app.workspace.world.links.count == 0 || turtleSet.count == 0)
      return Set()

    turtleSet.agents.asScala.toSeq.collect {
      case turtle: Turtle =>
        turtle.links.filter(!_.hidden)
    }.flatten.toSet
  }

  override def getObserverPerspectiveAngles: Array[Double] =
    getGLWindow.getObserverPerspectiveAngles

  override def getObserverPerspectiveFocusPoint: Array[Double] =
    getGLWindow.getObserverPerspectiveFocusPoint

  override def getObserverDistance: Double =
    getGLWindow.getObserverDistance

  override def shiftObserverFocusPoint(deltax: Double, deltay: Double): Unit = {
    getGLWindow.shiftObserverFocusPoint(deltax, deltay)
  }

  override def updateObserverPerspectiveAnglesWithDeltas(thetax: Double, thetay: Double): Unit = {
    getGLWindow.updateObserverPerspectiveAnglesWithDeltas(thetax, thetay)
  }

  override def zoomToDistance(dist: Double): Unit = {
    getGLWindow.zoomToDistance(dist)
  }





}
