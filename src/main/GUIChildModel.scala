package org.nlogo.ls

import java.awt._
import java.awt.event.{ WindowAdapter, WindowEvent }
import javax.swing.{ JFrame, JMenuBar, WindowConstants }
import java.io.{ FileNotFoundException, IOException }
import java.nio.file.{ Files, Paths }

import org.nlogo.api._
import org.nlogo.app.{ App, ZoomMenu }
import org.nlogo.ls.gui.{ GUIPanel, InterfaceComponent }
import org.nlogo.nvm.HaltException
import org.nlogo.swing.{ ModalProgress, NetLogoIcon, Utils }
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.GUIWorkspace

import scala.util.{ Failure, Try }

class GUIChildModel @throws(classOf[InterruptedException]) @throws(classOf[ExtensionException]) @throws(classOf[HaltException]) @throws(classOf[IOException])
(ls: LevelSpace, parentWorkspace: Workspace, path: String, modelID: Int)
  extends ChildModel(parentWorkspace, modelID) {

  if (!Files.exists(Paths.get(path))) {
    throw new FileNotFoundException(path)
  }

  val (component, panel, frame) = UnlockAndBlock.onEDT(parentWorkspace.world) {
    val f = new JFrame with NetLogoIcon with ModalProgress
    val component = new InterfaceComponent(f)
    val panel = new GUIPanel(component.workspace, component)
    (component, panel, Some(f))
  }

  var menuBar = new SyncedMenuBar()

  UnlockAndBlock.onEDT(parentWorkspace.world) (Try {
    val f = frame.get
    f.add(panel)
    val currentlyFocused: Window =
      Option(KeyboardFocusManager.getCurrentKeyboardFocusManager.getActiveWindow).getOrElse(App.app.frame)
    f.setLocationRelativeTo(currentlyFocused)
    f.setLocationByPlatform(true)
    f.setVisible(true)
    currentlyFocused.toFront()
    openModelWithoutGenerator(component.open, path)
    panel.packSplitPane()
    f.pack()
    panel.resetCommandCenter()
    f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)
    f.addWindowListener(new GUIWindowAdapter)
    f.setJMenuBar(menuBar)
    (component, panel)
  }) match {
    case Failure(_) => kill()
    case _ =>
  }
  updateFrameTitle()
  syncTheme()

  class GUIWindowAdapter extends WindowAdapter {
    override def windowClosing(windowEvent: WindowEvent): Unit = hide()
  }

  def setSpeed(d: Double): Unit = {
    // Note the panel will update and store the speed even if the frame is not visible. The thing is, we don't want
    // to slow the model down if it's not visible
    workspace.updateManager.speed = d
    // Wakes up the workspace if the speed slider is super low.
    // Makes it so there's not a long pause after increasing
    // the speed slider from a low position. BCH 6/18/2016
    workspace.updateManager.nudgeSleeper()
    onEDT {
      panel.speedSlider.setValue((d * 2).intValue)
    }
  }

  override def hide(): Unit = {
    workspace.updateManager.speed = 50
    workspace.updateManager.nudgeSleeper()
    super.hide()
  }

  override def show(): Unit = {
    workspace.updateManager.speed = panel.speedSlider.getValue / 2.0
    syncTheme()
    super.show()
  }

  def workspace: GUIWorkspace = component.workspace

  def syncTheme(): Unit = {
    menuBar.syncTheme()
    panel.syncTheme()

    frame.foreach(_.repaint())
  }
}

class SyncedMenuBar extends JMenuBar {
  private val zoomMenu = new ZoomMenu

  add(zoomMenu)

  override def paintComponent(g: Graphics): Unit = {
    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(InterfaceColors.menuBackground())
    g2d.fillRect(0, 0, getWidth, getHeight)
  }

  override def paintBorder(g: Graphics): Unit = {
    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(InterfaceColors.menuBarBorder())
    g2d.drawLine(0, getHeight - 1, getWidth, getHeight - 1)
  }

  def syncTheme(): Unit = {
    zoomMenu.syncTheme()
  }
}
