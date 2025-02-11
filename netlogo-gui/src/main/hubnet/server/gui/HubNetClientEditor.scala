// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server.gui

import java.awt.{ BorderLayout, Component, Dimension, GridBagConstraints, GridBagLayout, Insets }
import javax.swing.{ JFrame, JMenuBar, ScrollPaneConstants }

import org.nlogo.api.ModelType
import org.nlogo.core.{ I18N, Widget => CoreWidget }
import org.nlogo.swing.{ NetLogoIcon, ScrollPane, ToolBar }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ WidgetInfo, MenuBarFactory, InterfaceFactory, GUIWorkspace, AbstractWidgetPanel }

class HubNetClientEditor(workspace: GUIWorkspace,
                         linkParent: Component,
                         iFactory: InterfaceFactory,
                         menuFactory: MenuBarFactory) extends JFrame
        with org.nlogo.window.Event.LinkChild
        with org.nlogo.window.Events.ZoomedEvent.Handler
        with ThemeSync
        with NetLogoIcon {
  val interfacePanel: AbstractWidgetPanel = iFactory.widgetPanel(workspace)
  private val scrollPane = new ScrollPane(interfacePanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
    setBorder(null)
  }

  private val widgetControls = {
    import WidgetInfo._

    val buttons = List(button, slider, switch, chooser, input, monitor, plot, note, view)

    iFactory.widgetControls(interfacePanel, workspace, buttons, HubNetClientEditor.this)
  }

  private val toolbar = new ToolBar {
    setLayout(new GridBagLayout)

    override def addControls(): Unit = {
      val c = new GridBagConstraints

      c.anchor = GridBagConstraints.WEST
      c.weightx = 1
      c.insets = new Insets(6, 0, 6, 0)

      add(widgetControls, c)
    }
  }

  locally {
    setTitle(getTitle(workspace.modelNameForDisplay, workspace.getModelDir, workspace.getModelType))
    getContentPane.setLayout(new BorderLayout())
    getContentPane.add(scrollPane, BorderLayout.CENTER)
    getContentPane.add(toolbar, BorderLayout.NORTH)
    if (System.getProperty("os.name").startsWith("Mac")) {
      val menus = new JMenuBar() {add(menuFactory.createFileMenu)}
      val edit = menuFactory.createEditMenu
      edit.setEnabled(false)
      menus.add(edit)
      menus.add(menuFactory.createToolsMenu)
      menus.add(menuFactory.createZoomMenu)
      val helpMenu = menuFactory.createHelpMenu
      menus.add(helpMenu)
      try {
        menus.setHelpMenu(helpMenu)
      } catch {
        case e: Error => org.nlogo.api.Exceptions.ignore(e)
      }
      setJMenuBar(menus)
    }
    setSize(getPreferredSize)
  }

  override def getPreferredSize = if (interfacePanel.empty) new Dimension(700, 550) else super.getPreferredSize
  def getLinkParent = linkParent
  def close(): Unit = {interfacePanel.removeAllWidgets()}
  override def requestFocus(): Unit = {interfacePanel.requestFocus()}
  def getWidgetsForSaving: Seq[CoreWidget] = interfacePanel.getWidgetsForSaving

  def interfaceWidgets: Seq[CoreWidget] =
    interfacePanel.getWidgetsForSaving

  def load(widgets: Seq[CoreWidget]): Unit = {
    interfacePanel.loadWidgets(widgets)
    setSize(getPreferredSize)
  }

  def handle(e: org.nlogo.window.Events.ZoomedEvent): Unit = {setSize(getPreferredSize)}
  def setTitle(title: String, directory: String, mt: ModelType): Unit = {setTitle(getTitle(title, directory, mt))}

  private def getTitle (title:String, directory:String, mt: ModelType) = {
    // on OS X, use standard window title format. otherwise use Windows convention
    val t = if (!System.getProperty("os.name").startsWith("Mac")) {
      title + " - " + I18N.gui.get("menu.tools.hubNetClientEditor")
    } else {
      // 8212 is the unicode value for an em dash. we use the number since
      // we don't want non-ASCII characters in the source files -- AZS 6/14/2005
      I18N.gui.get("menu.tools.hubNetClientEditor") + " " + 8212.toChar + " " + title
    }
    // OS X UI guidelines prohibit paths in title bars, but oh well...
    if (mt == ModelType.Normal) t + " {" + directory + "}" else t
  }

  override def syncTheme(): Unit = {
    interfacePanel.syncTheme()

    scrollPane.setBackground(InterfaceColors.interfaceBackground)

    widgetControls match {
      case ts: ThemeSync => ts.syncTheme()
    }

    toolbar.setBackground(InterfaceColors.toolbarBackground)

    repaint()
  }
}
