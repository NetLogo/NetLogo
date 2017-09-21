// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.agent.{ Agent, Patch, Turtle }
import org.nlogo.core.{ AgentKind, I18N, Shape }
import org.nlogo.api.{ AgentException, AgentFollowingPerspective, Exceptions, Perspective,
  RendererInterface, ViewSettings }
import org.nlogo.awt.{ Colors, Hierarchy, ImageSelection }
import org.nlogo.window.Events.{ DirtyEvent, LoadBeginEvent, LoadEndEvent, CompiledEvent, IconifiedEvent }

import javax.swing.{ JComponent, JMenu, JMenuItem, JPopupMenu }
import java.awt.{ Component, Dimension, Font, Graphics, Graphics2D, Image, Point, Rectangle, Toolkit }
import java.awt.event.{ ActionEvent, ActionListener }
import java.awt.image.BufferedImage

object View {
  object AgentMenuType extends Enumeration {
    type AgentMenuType = Value
    val Inspect, Follow, Watch = Value
  }
}

import View.AgentMenuType._

@scala.annotation.strictfp
class View(val workspace: GUIWorkspaceScala) extends JComponent
    with LoadBeginEvent.Handler
    with LoadEndEvent.Handler
    with CompiledEvent.Handler
    with IconifiedEvent.Handler
    with ViewSettings
    with ActionListener
    with LocalViewInterface {

  private val mouser = new ViewMouseHandler(this, workspace.world, this)

  var frameCount: Int = 0
  var renderer: RendererInterface = workspace.newRenderer()
  var _renderPerspective: Boolean = true

  protected var _patchSize: Double = 13.0
  protected var _viewHeight: Double = 0.0
  protected var _viewWidth: Double = 0.0

  private var frozen: Boolean = false
  private var offscreenImage: Image = null
  private var gOff: Graphics2D = null
  private var zoom: Double = 1.0

  private var _dirty: Boolean = true // is the offscreen image out of date?
  private var _framesSkipped: Boolean = false
  private var _fontSize: Int = 13
  private var _iconified: Boolean = false
  private var _paintingImmediately: Boolean = false

  def iconified: Boolean = _iconified

  locally {
    setOpaque(true)
    addMouseListener(mouser)
    addMouseMotionListener(mouser)
    workspace.viewManager.add(this)
  }

  def isHeadless: Boolean = false

  def incrementalUpdateFromEventThread(): Unit = {
    _paintingImmediately = true
    paintImmediately()
    _paintingImmediately = false
  }

  def dirty(): Unit = {
    _dirty = true
  }

  def framesSkipped(): Unit = {
    _framesSkipped = true
  }

  def fontSize: Int = _fontSize

  protected def setTrueFontSize(size: Int): Unit = {
    _fontSize = size;
  }

  def resetMouseCors(): Unit = {
    mouser.resetMouseCors()
  }

  def mouseXCor: Double = mouser.mouseXCor
  def mouseYCor: Double = mouser.mouseYCor
  def mouseDown: Boolean = mouser.mouseDown
  def mouseInside: Boolean = mouser.mouseInside

  def mouseDown(mouseDown: Boolean): Unit = {
    mouser.mouseDown(mouseDown)
  }

  def isDead: Boolean = false

  /// sizing

  override def getMinimumSize: Dimension =
    new Dimension(workspace.world.worldWidth, workspace.world.worldHeight)

  override def getPreferredSize: Dimension = {
    val width = (viewWidth * patchSize).toInt
    val height = (viewHeight * patchSize).toInt
    new Dimension(width, height)
  }

  /// iconified checking

  def handle(e: IconifiedEvent): Unit =
    if (e.frame == Hierarchy.getFrame(this)) {
      _iconified = e.iconified
    }

  private def beClean(): Boolean = { // return = success true/false
    // it used to be ok for the height and width to be 0
    // as this method would never get called but
    // now setFont calls it and forces the image to be created
    if (_dirty && getWidth > 0 && getHeight > 0) {
      // this check fixes a bug where during halting,
      // the event thread would wait forever for the world
      // lock. probably not a 100% correct fix, but an
      // improvement, at least - ST 1/10/07
      if (workspace.jobManager.isInterrupted) {
        return false
      }
      if (offscreenImage == null) {
        offscreenImage = createImage(getWidth, getHeight)
        if (offscreenImage != null) {
          gOff = offscreenImage.getGraphics.asInstanceOf[Graphics2D]
          gOff.setFont(getFont)
        }
      }
      // this might happen since the view widget is not displayable in 3D ev 7/5/07
      if (gOff != null) {
        workspace.world.synchronized {
          renderer.paint(gOff, this)
        }
      }
      _dirty = false
    }
    true
  }

  @Override
  override def setBounds(x: Int, y: Int, width: Int, height: Int): Unit = {
    val bounds = getBounds()
    // only set the bounds if they've changed
    if (width != bounds.width || height != bounds.height || x != bounds.x || y != bounds.y) {
      super.setBounds(x, y, width, height)
      discardOffscreenImage()
    }
  }

  def discardOffscreenImage(): Unit = {
    offscreenImage = null
    gOff = null
    _dirty = true
  }

  override def setBounds(bounds: Rectangle): Unit = {
    setBounds(bounds.x, bounds.y, bounds.width, bounds.height)
  }

  /// painting

  override def paint(g: Graphics): Unit = {
    if (!isDead) {
      workspace.updateManager.beginPainting()
      super.paint(g)
      workspace.updateManager.donePainting()

      // update the mouse coordinates if following
      if (workspace.world.observer.perspective.isInstanceOf[AgentFollowingPerspective]) {
        mouser.updateMouseCors()
      }
    }
  }

  override def paintComponent(g: Graphics): Unit = {
    frameCount += 1
    if (frozen || workspace.displayStatus.renderAsGray) {
      if (_dirty) {
        g.setColor(InterfaceColors.GRAPHICS_BACKGROUND)
        g.fillRect(0, 0, getWidth, getHeight)
      } else {
        g.drawImage(offscreenImage, 0, 0, null)
      }
      _framesSkipped = false
    } else if (_paintingImmediately) {
      workspace.world.synchronized {
        renderer.paint(g.asInstanceOf[Graphics2D], this)
      }
      _framesSkipped = false
    } else {
      if (beClean()) {
        g.drawImage(offscreenImage, 0, 0, null)
        _framesSkipped = false
      } else {
        _framesSkipped = true
      }
    }
  }

  def paintingImmediately(paintingImmediately: Boolean): Unit = {
    _paintingImmediately = paintingImmediately
  }

  def paintingImmediately: Boolean = _paintingImmediately

  def paintImmediately(force: Boolean): Unit = {
    if (viewIsVisible && (_framesSkipped || force)) {
      paintingImmediately(true)
      paintImmediately()
      paintingImmediately(false)
    }
  }

  def viewIsVisible: Boolean = ! _iconified && isShowing

  def paintImmediately(): Unit = {
    paintImmediately(0, 0, getWidth, getHeight)
  }

  def exportView(): BufferedImage = {
    // unfortunately we can't just call awt.Utils.paintToImage()
    // here because we need to do a few nonstandard things
    // (namely call renderer's paint method instead of
    // our own, and grab the world lock) - ST 6/12/04
    val image = new BufferedImage(getWidth, getHeight, BufferedImage.TYPE_INT_ARGB)
    val graphics = image.getGraphics.asInstanceOf[java.awt.Graphics2D];
    graphics.setFont(getFont)
    workspace.world.synchronized {
      renderer.paint(graphics, this)
    }
    image
  }

  /// freeze/thaw

  def freeze(): Unit = {
    if (!frozen) {
      frozen = true
      if (workspace.displayStatus.shouldRender(false)) {
        beClean()
      }
    }
  }

  def thaw(): Unit = {
    if (frozen) {
      frozen = false
      repaint()
    }
  }

  /// shapes

  // for notification from ShapesManager
  def shapeChanged(shape: Shape): Unit = {
    _dirty = true
    new DirtyEvent(None).raise(this)
    renderer.resetCache(patchSize)
    repaint()
  }

  /// event handlers

  def handle(e: LoadBeginEvent): Unit = {
    setVisible(false)
    renderer = workspace.newRenderer()
  }

  def handle(e: LoadEndEvent): Unit = {
    renderer.changeTopology(workspace.world.wrappingAllowedInX, workspace.world.wrappingAllowedInY)
    setVisible(true)
  }

  def handle(e: CompiledEvent): Unit = {
    e.sourceOwner match {
      case p: ProceduresInterface => renderer.resetCache(patchSize)
      case _ =>
    }
  }

  override def setVisible(visible: Boolean): Unit = {
    super.setVisible(visible)
    if (visible) {
      _dirty = true
      beClean()
    }
  }

  // Our Rendered gets Font size from the graphics context it is
  // given so we need to make sure our offScreenImage's graphics
  // context has our new font size. -- CLB
  override def setFont(font: Font): Unit = {
    super.setFont(font)
    discardOffscreenImage()
  }

  def applyNewFontSize(newFontSize: Int, zoom: Int): Unit = {
    val font = getFont()
    val newFont = new Font(font.getName, font.getStyle, (newFontSize + zoom))
    setTrueFontSize(newFontSize)
    setFont(newFont)
    dirty()
    repaint()
  }

  def getExportWindowFrame: Component = workspace.viewWidget

  def patchSize: Double = _patchSize * zoom

  def visualPatchSize(patchSize: Double): Unit = {
    val oldZoom = zoom
    zoom = patchSize / _patchSize
    if (zoom != oldZoom) {
      renderer.resetCache(patchSize)
    }
  }

  def viewWidth: Double = _viewWidth
  def viewHeight: Double = _viewHeight

  def setSize(worldWidth: Int, worldHeight: Int, patchSize: Double): Unit = {
    _patchSize = patchSize
    _viewWidth = worldWidth
    _viewHeight = worldHeight

    renderer.resetCache(patchSize)
  }

  def perspective: Perspective = workspace.world.observer.perspective
  def drawSpotlight: Boolean = true

  def viewOffsetX: Double = workspace.world.observer.followOffsetX
  def viewOffsetY: Double = workspace.world.observer.followOffsetY

  def renderPerspective_=(b: Boolean): Unit = {
    _renderPerspective = b
  }
  def renderPerspective: Boolean = _renderPerspective

  def populateContextMenu(menu: JPopupMenu, p: Point, source: Component): Point = {
    // certain menu items dont work in Applets.
    // the only ones that do are watch, follow and reset-perspective
    // this check (and others below) prevent items from being added
    // when we are running in Applet. JC - 6/8/10
    val copyItem = new JMenuItem(I18N.gui.get("tabs.run.widget.view.copy"))
    copyItem.addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent): Unit = {
        Toolkit.getDefaultToolkit.getSystemClipboard.setContents(new ImageSelection(exportView()), null)
      }
    })
    menu.add(copyItem)
    val exportItem = new JMenuItem(I18N.gui.get("tabs.run.widget.view.export"))
    exportItem.addActionListener(new ActionListener() {
      def actionPerformed(e: ActionEvent): Unit = {
        workspace.doExportView(View.this)
      }
    });
    menu.add(exportItem)

    val open3DView = new JMenuItem(workspace.switchTo3DViewAction)
    menu.add(open3DView)

    menu.add(new JPopupMenu.Separator())

    val inspectGlobalsItem = new JMenuItem(I18N.gui.get("tabs.run.widget.view.inspectGlobals"))
    inspectGlobalsItem.addActionListener(new ActionListener() {
      def actionPerformed(actionEvent: ActionEvent): Unit = {
        workspace.inspectAgent(AgentKind.Observer)
      }
    })
    menu.add(inspectGlobalsItem)

    if (!workspace.world.observer.atHome2D) {
      menu.add(new JPopupMenu.Separator())
      val resetItem =
        new JMenuItem("<html>" + org.nlogo.awt.Colors.colorize("reset-perspective", SyntaxColors.COMMAND_COLOR))
      resetItem.addActionListener(new ActionListener() {
        def actionPerformed(e: ActionEvent): Unit = {
          workspace.world.observer.resetPerspective()
          workspace.viewManager.incrementalUpdateFromEventThread()
        }
      })
      menu.add(resetItem)
    }
    val newP = new Point(p)
    mouser.translatePointToXCorYCor(newP)
    workspace.world.synchronized {
      val xcor = mouser.translatePointToUnboundedX(newP.x)
      val ycor = mouser.translatePointToUnboundedY(newP.y)

      var patch: Patch = null

      try {
        patch = workspace.world.getPatchAt(xcor, ycor)
        menu.add(new JPopupMenu.Separator())
        menu.add(new AgentMenuItem(patch, Inspect, "inspect", false))
      } catch {
        case e: AgentException => Exceptions.ignore(e)
      }

      var linksAdded = false
      val links = workspace.world.links.iterator
      while (links.hasNext) {
        val link = links.next().asInstanceOf[org.nlogo.agent.Link]

        if (!link.hidden && workspace.world.protractor.distance(link, xcor, ycor, true) < link.lineThickness + 0.5) {
          if (!linksAdded) {
            menu.add(new JPopupMenu.Separator())
            linksAdded = true
          }
          menu.add(new AgentMenuItem(link, Inspect, "inspect", false))
        }
      }

      // detect any turtles in the pick-ray
      var turtlesAdded = false
      val turtles = workspace.world.turtles.iterator
      while (turtles.hasNext) {
        val turtle =  turtles.next().asInstanceOf[Turtle]
        if (!turtle.hidden) {
          var offset = turtle.size * 0.5
          if (offset * workspace.world.patchSize < 3) {
            offset += (3 / workspace.world.patchSize)
          }

          val shape = workspace.world.turtleShapeList.shape(turtle.shape).asInstanceOf[org.nlogo.shape.VectorShape]

          if (shape.isRotatable && !turtle.hidden) {
            val dist = workspace.world.protractor.distance(turtle, xcor, ycor, true)

            if (dist <= offset) {
              if (!turtlesAdded) {
                menu.add(new JPopupMenu.Separator())
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

            if (workspace.world.wrappingAllowedInX) {
              val x =
                if (xCor > xMouse) xMouse + workspace.world.worldWidth
                else               xMouse - workspace.world.worldWidth
              xMouse =
                if (StrictMath.abs(xMouse - xCor) < StrictMath.abs(x - xCor)) xMouse
                else                                                          x
            }
            if (workspace.world.wrappingAllowedInY) {
              val y =
                if (yCor > yMouse) yMouse + workspace.world.worldHeight
                else               yMouse - workspace.world.worldHeight
              yMouse =
                if (StrictMath.abs(yMouse - yCor) < StrictMath.abs(y - yCor)) yMouse
                else                                                          y
            }

            if ((xMouse >= xCor - offset) && (xMouse <= xCor + offset) &&
                (yMouse >= yCor - offset) && (yMouse <= yCor + offset)) {
              if (!turtlesAdded) {
                menu.add(new JPopupMenu.Separator())
                turtlesAdded = true
              }

              addTurtleToContextMenu(menu, turtle)
            }
          }
        }
      }

      var x = 0
      var y = 0

      if (patch != null) {
        x = StrictMath.round(renderer.graphicsX(patch.pxcor + 1, patchSize, viewOffsetX)).toInt;
        y = StrictMath.round(renderer.graphicsY(patch.pycor - 1, patchSize, viewOffsetY)).toInt;

        newP.x += StrictMath.min((x - newP.x), 15)
        newP.y += StrictMath.min((y - newP.y), 15)
      }
    }

    newP
  }

  private def addTurtleToContextMenu(menu: JPopupMenu, turtle: Turtle): Unit = {
    val submenu = new AgentMenu(turtle)
    submenu.add(new AgentMenuItem(turtle, Inspect, "inspect", true))
    submenu.add(new JPopupMenu.Separator())
    submenu.add(new AgentMenuItem(turtle, Watch, "watch", true))
    submenu.add(new AgentMenuItem(turtle, Follow, "follow", true))
    menu.add(submenu)
  }

  /// context menu

  object AgentMenuItem {
    def prepText(agent: Agent, caption: String): String = {
      "<html>" +
      Colors.colorize(caption, SyntaxColors.COMMAND_COLOR) +
      " " +
      Colors.colorize(agent.classDisplayName, SyntaxColors.REPORTER_COLOR) +
      Colors.colorize(agent.toString.substring(agent.classDisplayName.length), SyntaxColors.CONSTANT_COLOR)
    }
  }
  private class AgentMenuItem(val agent: Agent, val tpe: AgentMenuType, caption: String, submenu: Boolean)
  extends JMenuItem(AgentMenuItem.prepText(agent, caption)) {
    addActionListener(View.this)

    override def menuSelectionChanged(isIncluded: Boolean): Unit = {
      super.menuSelectionChanged(isIncluded)
      if (!submenu) {
        renderer.outlineAgent(if (isIncluded) agent else null)
        workspace.viewManager.incrementalUpdateFromEventThread()
      }
    }
  }

  private class AgentMenu(agent: Agent) extends JMenu(agent.toString) {
    override def menuSelectionChanged(isIncluded: Boolean): Unit = {
      super.menuSelectionChanged(isIncluded)
      renderer.outlineAgent(if (isIncluded) agent else null)
      workspace.viewManager.incrementalUpdateFromEventThread()
    }
  }

  def actionPerformed(e: ActionEvent): Unit = {
    val item = e.getSource.asInstanceOf[AgentMenuItem]
    item.tpe match {
      case Inspect =>
        // we usually use a default radius of 3, but that doesnt work when the world
        // has a radius of less than 3. so simply take the miniumum. - JC 7/1/10
        val minWidthOrHeight = StrictMath.min(workspace.world.worldWidth / 2, workspace.world.worldHeight / 2)
        val radius = StrictMath.min(3, minWidthOrHeight / 2)
        workspace.inspectAgent(item.agent.kind, item.agent, radius)
      case Follow =>
        val distance = (item.agent.asInstanceOf[org.nlogo.agent.Turtle].size * 5).toInt;
        val newPerspective = Perspective.Follow(item.agent, StrictMath.max(1, StrictMath.min(distance, 100)))
        workspace.world.observer.setPerspective(newPerspective)
      case Watch =>
        workspace.world.observer.home()
        val newPerspective = Perspective.Watch(item.agent)
        workspace.world.observer.setPerspective(newPerspective)
    }

    workspace.viewManager.incrementalUpdateFromEventThread()
  }
}
