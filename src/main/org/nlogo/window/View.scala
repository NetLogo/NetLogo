// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.{ JComponent, JMenu, JMenuItem, JPopupMenu }
import java.awt.{ Component, Dimension, Font, Graphics, Graphics2D, Image, Point, Rectangle, Toolkit }
import java.awt.event.{ ActionEvent, ActionListener }
import java.awt.image.BufferedImage
import org.nlogo.agent.{ Agent, AgentSet, Link, Observer, Patch, Turtle }
import org.nlogo.api.{ AgentException, AgentKind, Perspective, RendererInterface, Shape, ViewSettings }
import org.nlogo.awt.{ Colors, Hierarchy, ImageSelection }
import org.nlogo.shape.VectorShape
import org.nlogo.util.Exceptions
import org.nlogo.workspace.AbstractWorkspace
import scala.collection.JavaConverters._

class View(protected val workspace: GUIWorkspace) extends JComponent
    with Events.LoadBeginEventHandler with Events.LoadEndEventHandler
    with Events.CompiledEventHandler with  Events.IconifiedEventHandler
    with ViewSettings with ActionListener with LocalViewInterface {
  var paintingImmediately = false
  private var _framesSkipped = false

  private var _fontSize = 13

  setOpaque(true)
  var renderer = workspace.newRenderer
  val mouser = new ViewMouseHandler(this, workspace.world, this)
  addMouseListener(mouser)
  addMouseMotionListener(mouser)
  workspace.viewManager.add(this)

  def isHeadless = workspace.isHeadless

  def displayOn = !workspace.glView.isFullscreen && workspace.world.displayOn && workspace.displaySwitchOn

  def displaySwitch = workspace.viewWidget.displaySwitch.isSelected
  def displaySwitch(on: Boolean) = workspace.viewWidget.displaySwitch.setOn(on)

  private val paintRunnable = new Runnable {
      def run() = {
        paintingImmediately = true
        paintImmediately()
        paintingImmediately = false
      }
    }

  def incrementalUpdateFromEventThread() = paintRunnable.run()

  def dirty() = _dirty = true
  def framesSkipped() = _framesSkipped = true

  def fontSize = _fontSize

  def resetMouseCors() = mouser.resetMouseCors()

  def mouseXCor = mouser.mouseXCor
  def mouseYCor = mouser.mouseYCor
  def mouseDown = mouser.mouseDown
  def mouseDown_=(mouseDown: Boolean) = mouser.mouseDown = mouseDown
  def mouseInside = mouser.mouseInside

  def isDead = false

  /// sizing

  override def getMinimumSize = new Dimension(workspace.world.worldWidth, workspace.world.worldHeight)

  override def getPreferredSize = {
    val width = (viewWidth * patchSize).toInt
    val height = (viewHeight * patchSize).toInt
    new Dimension(width, height)
  }

  /// iconified checking

  var iconified = false
  def handle(e: Events.IconifiedEvent) = if (e.frame == Hierarchy.getFrame(this)) iconified = e.iconified

  /// offscreen stuff

  private var offscreenImage: Image = null
  private var gOff: Graphics2D = null
  private var _dirty = true // is the offscreen image out of date?

  private def beClean(): Boolean = { // return = success true/false
    // it used to be ok for the height and width to be 0
    // as this method would never get called but
    // now setFont calls it and forces the image to be created
    if(_dirty && getWidth > 0 && getHeight > 0) {
      // this check fixes a bug where during halting,
      // the event thread would wait forever for the world
      // lock. probably not a 100% correct fix, but an
      // improvement, at least - ST 1/10/07
      if(workspace.jobManager.isInterrupted)
        return false
      if(offscreenImage == null) {
        offscreenImage = createImage(getWidth, getHeight)
        if(offscreenImage != null) {
          gOff = offscreenImage.getGraphics.asInstanceOf[Graphics2D]
          gOff.setFont(getFont)
        }
      }
      // this might happen since the view widget is not displayable in 3D ev 7/5/07
      if(gOff != null)
        workspace.world.synchronized(renderer.paint(gOff, this))
      _dirty = false
    }
    true
  }

  override def setBounds(x: Int, y: Int, width: Int, height: Int) = {
    val bounds = getBounds()
    // only set the bounds if they've changed
    if(width != bounds.width || height != bounds.height || x != bounds.x || y != bounds.y) {
      super.setBounds(x, y, width, height)
      discardOffscreenImage()
    }
  }

  def discardOffscreenImage() = {
    offscreenImage = null
    gOff = null
    dirty()
  }

  override def setBounds(bounds: Rectangle) = setBounds(bounds.x, bounds.y, bounds.width, bounds.height)

  /// painting

  var frameCount = 0

  override def paint(g: Graphics) = if(!isDead) {
      workspace.updateManager.beginPainting()
      super.paint(g)
      workspace.updateManager.donePainting()

      // update the mouse coordinates if following
      if(workspace.world.observer.perspective == Perspective.Follow ||
         workspace.world.observer.perspective == Perspective.Ride)
        mouser.updateMouseCors()
    }

  override def paintComponent(g: Graphics) = {
    frameCount += 1
    if(frozen || !workspace.world.displayOn) {
      if(_dirty) {
        g.setColor(InterfaceColors.GRAPHICS_BACKGROUND)
        g.fillRect(0, 0, getWidth, getHeight)
      } else {
        g.drawImage(offscreenImage, 0, 0, null)
      }
      _framesSkipped = false
    } else if(paintingImmediately) {
      workspace.world.synchronized(renderer.paint(g.asInstanceOf[Graphics2D], this))
      _framesSkipped = false
    } else {
      if(beClean) {
        g.drawImage(offscreenImage, 0, 0, null)
        _framesSkipped = false
      } else {
        framesSkipped()
      }
    }
  }

  def viewIsVisible = !iconified && isShowing
  def paintImmediately(): Unit = paintImmediately(0, 0, getWidth, getHeight)
  def paintImmediately(force: Boolean) = if(viewIsVisible && (_framesSkipped || force)) {
      paintingImmediately = true
      paintImmediately()
      paintingImmediately = false
    }

  def exportView = {
    // unfortunately we can't just call awt.Utils.paintToImage()
    // here because we need to do a few nonstandard things
    // (namely call renderer's paint method instead of
    // our own, and grab the world lock) - ST 6/12/04
    val image = new BufferedImage(getWidth, getHeight, BufferedImage.TYPE_INT_ARGB)
    val graphics = image.getGraphics.asInstanceOf[Graphics2D]
    graphics.setFont(getFont)
    workspace.world.synchronized(renderer.paint(graphics, this))
    image
  }

  /// freeze/thaw

  private var frozen = false

  def freeze() = if (!frozen) {
      frozen = true
      if (workspace.world.displayOn)
        beClean()
    }

  def thaw() = if (frozen) {
      frozen = false
      repaint()
    }

  /// shapes

  // for notification from ShapesManager
  def shapeChanged(shape: Shape) = {
    dirty()
    new Events.DirtyEvent().raise(this)
    renderer.resetCache(patchSize)
    repaint()
  }

  /// event handlers

  def handle(e: Events.LoadBeginEvent) = {
    setVisible(false)
    _patchSize = 13
    zoom = 0
    renderer = workspace.newRenderer
  }

  def handle(e: Events.LoadEndEvent) = {
    renderer.changeTopology(workspace.world.wrappingAllowedInX, workspace.world.wrappingAllowedInY)
    setVisible(true)
  }

  def handle(e: Events.CompiledEvent) = if(e.sourceOwner.isInstanceOf[ProceduresInterface])
      renderer.resetCache(patchSize)

  override def setVisible(visible: Boolean) = {
    super.setVisible(visible)
    if(visible) {
      dirty()
      beClean()
    }
  }

  def setTrueFontSize(size: Int) = _fontSize = size

  // Our Rendered gets Font size from the graphics context it is
  // given so we need to make sure our offScreenImage's graphics
  // context has our new font size. -- CLB
  override def setFont(font: Font) = {
    super.setFont(font)
    offscreenImage = null
    discardOffscreenImage()
  }

  def applyNewFontSize(newFontSize: Int, zoom: Int) = {
    val font = getFont
    val newFont = new Font(font.getName, font.getStyle, newFontSize + zoom)
    setTrueFontSize(newFontSize)
    setFont(newFont)
    dirty()
    repaint()
  }

  def getExportWindowFrame = workspace.viewWidget

  protected var _patchSize = 13: Double
  private var zoom = 0: Double

  def patchSize = _patchSize + zoom
  def unzoomedPatchSize = _patchSize

  def unzoomedFont = {
    val font = getFont
    new Font(font.getName, font.getStyle, fontSize)
  }

  def visualPatchSize_=(patchSize: Double) = {
    val oldZoom = zoom
    zoom = patchSize - this.patchSize
    if(zoom != oldZoom)
      renderer.resetCache(patchSize)
  }

  protected var _viewWidth = 0: Double
  protected var _viewHeight = 0: Double
  def viewWidth = _viewWidth
  def viewHeight = _viewHeight

  def setSize(worldWidth: Int, worldHeight: Int, patchSize: Double) = {
    _patchSize = patchSize
    _viewWidth = worldWidth
    _viewHeight = worldHeight

    renderer.resetCache(this.patchSize)
  }

  def setSize(worldWidth: Int, worldHeight: Int, viewHeight: Double, viewWidth: Double, patchSize: Double) {
    _patchSize = patchSize
    _viewWidth = viewWidth
    _viewHeight = viewHeight

    renderer.resetCache(this.patchSize)
  }

  def perspective = workspace.world.observer.perspective
  def drawSpotlight = true

  def viewOffsetX = workspace.world.observer.followOffsetX
  def viewOffsetY = workspace.world.observer.followOffsetY

  var renderPerspective = true

  def populateContextMenu(menu: JPopupMenu, _p: Point, source: Component) = {
    var p = _p
    // certain menu items dont work in Applets.
    // the only ones that do are watch, follow and reset-perspective
    // this check (and others below) prevent items from being added
    // when we are running in Applet. JC - 6/8/10
    if(!AbstractWorkspace.isApplet) {
      val copyItem = new JMenuItem("Copy View")
      copyItem.addActionListener(new ActionListener {
          def actionPerformed(e: ActionEvent) =
            Toolkit.getDefaultToolkit.getSystemClipboard.setContents(new ImageSelection(exportView),null)
        })
      menu.add(copyItem)
      val exportItem = new JMenuItem("Export View...")
      exportItem.addActionListener(new ActionListener {
          def actionPerformed(e: ActionEvent) = workspace.doExportView(View.this)
        })
      menu.add(exportItem)
    }

    menu.add(new JPopupMenu.Separator)
    val inspectGlobalsItem = new JMenuItem("inspect globals")
    inspectGlobalsItem.addActionListener(new ActionListener {
        def actionPerformed(e: ActionEvent) = workspace.inspectAgent(AgentKind.Observer)
      })
    menu.add(inspectGlobalsItem)

    if(!workspace.world.observer.atHome2D) {
      menu.add(new JPopupMenu.Separator)
      val resetItem =
        new JMenuItem(s"<html>${Colors.colorize("reset-perspective", SyntaxColors.COMMAND_COLOR)}")
      resetItem.addActionListener(new ActionListener {
            def actionPerformed(e: ActionEvent) = {
              workspace.world.observer.resetPerspective()
              workspace.viewManager.incrementalUpdateFromEventThread()
            }
          })
      menu.add(resetItem)
    }
    p = new Point(p)
    mouser.translatePointToXCorYCor(p)
    workspace.world.synchronized {
      val xcor = mouser.translatePointToUnboundedX(p.x)
      val ycor = mouser.translatePointToUnboundedY(p.y)

      var patch: Patch = null

      if(!AbstractWorkspace.isApplet) {
        try {
          patch = workspace.world.getPatchAt(xcor, ycor)
          menu.add(new JPopupMenu.Separator)
          menu.add(new AgentMenuItem(patch, AgentMenuType.Inspect, "inspect", false))
        } catch {
          case e: AgentException => Exceptions.ignore(e)
        }

        var linksAdded = false
        workspace.world.links.agents.asScala foreach { _link => val link = _link.asInstanceOf[Link]
          if(!link.hidden &&
             workspace.world.protractor.distance(link, xcor, ycor, true) < link.lineThickness + 0.5)
            if (!linksAdded) {
              menu.add(new JPopupMenu.Separator)
              linksAdded = true
            }
            menu.add(new AgentMenuItem(link, AgentMenuType.Inspect, "inspect", false))
        }
      }

      // detect any turtles in the pick-ray
      var turtlesAdded = false
      workspace.world.turtles.agents.asScala foreach { _turtle => val turtle = _turtle.asInstanceOf[Turtle]
        if(!turtle.hidden) {
          var offset = turtle.size * 0.5
          if(offset * workspace.world.patchSize < 3)
            offset += (3 / workspace.world.patchSize)

          val shape = workspace.world.turtleShapeList.shape(turtle.shape).asInstanceOf[VectorShape]

          if(shape.isRotatable) {
            val dist = workspace.world.protractor.distance(turtle, xcor, ycor, true)

            if(dist <= offset) {
              if(!turtlesAdded) {
                menu.add(new JPopupMenu.Separator)
                turtlesAdded = true
              }
              addTurtleToContextMenu(menu, turtle)
            }
          } else {
            // otherwise the turtle takes a square shape
            val xCor = turtle.xcor
            val yCor = turtle.ycor
            var xMouse = xcor
            var yMouse = ycor

            if(workspace.world.wrappingAllowedInX) {
              val x =
                xMouse + (if(xCor > xMouse) workspace.world.worldWidth else -workspace.world.worldWidth)
              if(StrictMath.abs(xMouse - xCor) >= StrictMath.abs(x - xCor))
                xMouse = x
            }
            if(workspace.world.wrappingAllowedInX) {
              val y =
                yMouse + (if(yCor > yMouse) workspace.world.worldHeight else -workspace.world.worldHeight)
              if(StrictMath.abs(yMouse - yCor) >= StrictMath.abs(y - yCor))
                yMouse = y
            }

            if(xMouse >= xCor - offset && xMouse <= xCor + offset &&
               yMouse >= yCor - offset && yMouse <= yCor + offset) {
              if (!turtlesAdded) {
                menu.add(new JPopupMenu.Separator)
                turtlesAdded = true
              }
              addTurtleToContextMenu(menu, turtle)
            }
          }
        }
      }

      var x = 0
      var y = 0

      if(patch != null) {
        x = StrictMath.round(renderer.graphicsX(patch.pxcor + 1, patchSize, viewOffsetX)).toInt
        y = StrictMath.round(renderer.graphicsY(patch.pycor - 1, patchSize, viewOffsetY)).toInt

        p.x += StrictMath.min((x - p.x), 15)
        p.y += StrictMath.min((y - p.y), 15)
      }
    }
    p
  }

  private def addTurtleToContextMenu(menu: JPopupMenu, turtle: Turtle) {
    val submenu = new AgentMenu(turtle)
    if(!AbstractWorkspace.isApplet) {
      submenu.add(new AgentMenuItem(turtle, AgentMenuType.Inspect, "inspect", true))
      submenu.add(new JPopupMenu.Separator)
    }
    submenu.add(new AgentMenuItem(turtle, AgentMenuType.Watch, "watch", true))
    submenu.add(new AgentMenuItem(turtle, AgentMenuType.Follow, "follow", true))
    menu.add(submenu)
  }

  /// context menu

  object AgentMenuType extends Enumeration {
    val Inspect, Follow, Watch = Value
  }

  private class AgentMenuItem(val agent: Agent, val tpe: AgentMenuType.Value,
    caption: String, submenu: Boolean)
      extends JMenuItem("<html>" +
        Colors.colorize(caption, SyntaxColors.COMMAND_COLOR) + ' ' +
        Colors.colorize(agent.classDisplayName, SyntaxColors.REPORTER_COLOR) +
        Colors.colorize(agent.toString.substring(agent.classDisplayName.length),
          SyntaxColors.CONSTANT_COLOR)) {
    addActionListener(View.this)

    override def menuSelectionChanged(isIncluded: Boolean) = {
      super.menuSelectionChanged(isIncluded)
      if(!submenu) {
        renderer.outlineAgent(if(isIncluded) agent else null)
        workspace.viewManager.incrementalUpdateFromEventThread()
      }
    }
  }

  private class AgentMenu(agent: Agent) extends JMenu(agent.toString) {
    override def menuSelectionChanged(isIncluded: Boolean) = {
      super.menuSelectionChanged(isIncluded)
      renderer.outlineAgent(if(isIncluded) agent else null)
      workspace.viewManager.incrementalUpdateFromEventThread()
    }
  }

  def actionPerformed(e: ActionEvent): Unit = {
    import AgentMenuType._

    val item = e.getSource.asInstanceOf[AgentMenuItem]
    item.tpe match {
      case Inspect =>
        // we usually use a default radius of 3, but that doesnt work when the world
        // has a radius of less than 3. so simply take the miniumum. - JC 7/1/10
        val minWidthOrHeight =
          StrictMath.min(workspace.world.worldWidth / 2, workspace.world.worldHeight / 2)
        val radius = StrictMath.min(3, minWidthOrHeight / 2)
        workspace.inspectAgent(item.agent.kind, item.agent, radius)
        return
      case Follow =>
        workspace.world.observer.setPerspective(Perspective.Follow, item.agent)
        val distance = item.agent.asInstanceOf[Turtle].size.toInt * 5
        workspace.world.observer.followDistance = StrictMath.max(1, StrictMath.min(distance, 100))
      case Watch =>
        workspace.world.observer.home()
        workspace.world.observer.setPerspective(Perspective.Watch, item.agent)
      case _ => throw new IllegalStateException
    }
    workspace.viewManager.incrementalUpdateFromEventThread()
  }
}
