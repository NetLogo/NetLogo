// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Frame
import java.awt.event.{ ActionEvent, KeyEvent }
import java.nio.file.Path
import javax.swing.{ AbstractAction, JDialog }

import org.nlogo.api.{ AggregateManagerInterface, LibraryManager }
import org.nlogo.app.common.AppTabsInterface
import org.nlogo.app.interfacetab.WidgetPanel
import org.nlogo.app.tools.{ LibrariesDialog, PreferencesDialog }
import org.nlogo.awt.Positioning
import org.nlogo.core.{ LocaleContext, Token }
import org.nlogo.shape.ShapesManagerInterface
import org.nlogo.swing.{ OptionPane, UserAction }, UserAction._
import org.nlogo.theme.ThemeSync
import org.nlogo.window.{ AbstractWidgetPanel, CopyOnly, JFXColorPicker, LinkRoot, NLNumber }
import org.nlogo.workspace.AbstractWorkspaceScala

abstract class ShowDialogAction(name: String) extends AbstractAction(name) with ThemeSync {
  protected def createDialog(): JDialog & ThemeSync

  lazy protected val createdDialog = createDialog()

  override def actionPerformed(e: ActionEvent): Unit = {
    createdDialog.toFront()
    createdDialog.setVisible(true)
  }

  override def syncTheme(): Unit = {
    createdDialog.syncTheme()
  }
}

class ShowPreferencesDialog(frame: Frame & ThemeSync, tabs: AppTabsInterface, widgetPanel: AbstractWidgetPanel)
extends AbstractAction(loc"Preferences") with ThemeSync
with MenuAction {
  category = ToolsCategory
  group    = ToolsSettingsGroup
  mnemonic = 'P'

  private lazy val dialog = new PreferencesDialog(frame, tabs, widgetPanel)

  override def actionPerformed(e: ActionEvent): Unit = {
    openToTab(0)
  }

  override def syncTheme(): Unit = {
    dialog.syncTheme()
  }

  def openToTab(index: Int): Unit = {
    dialog.setSelectedIndex(index)
    dialog.toFront()
    dialog.setVisible(true)
  }
}

class OpenLibrariesDialog( frame:              Frame
                         , libManager:         LibraryManager
                         , recompile:          () => Unit
                         , tokenizeSource:     String => Iterator[Token]
                         , updateSource:       ((String) => String) => Unit
                         , getExtPathMappings: () => Map[String, Path]
                         ) extends ShowDialogAction(loc"Extensions") with MenuAction {

  category = ToolsCategory
  group    = ToolsSettingsGroup
  mnemonic = 'E'

  def createDialog() = new LibrariesDialog(frame, libManager, recompile, tokenizeSource, updateSource,
                                           getExtPathMappings())

}

class OpenRGBAColorDialog(frame: Frame) extends ShowDialogAction(loc"Color Picker") with MenuAction {
  category = ToolsCategory
  group = ToolsDialogsGroup
  mnemonic = 'C'

  def createDialog() = new JFXColorPicker(frame, false, CopyOnly, Option(NLNumber(15)))

  override def actionPerformed(e: ActionEvent): Unit = {
    Positioning.center(createdDialog, frame)

    super.actionPerformed(e)
  }
}

class ShowShapeManager(name: String, shapeManager: => ShapesManagerInterface)
extends AbstractAction(name)
with MenuAction {
  category = ToolsCategory
  group    = ToolsDialogsGroup

  override def actionPerformed(e: ActionEvent): Unit = {
    shapeManager.init(name)
  }
}

class ShowSystemDynamicsModeler(aggregateManager: AggregateManagerInterface)
extends AbstractAction(loc"System Dynamics Modeler")
with MenuAction {
  category    = ToolsCategory
  group       = ToolsDialogsGroup
  accelerator = KeyBindings.keystroke('D', withMenu = true, withShift = true)
  mnemonic    = KeyEvent.VK_D

  override def actionPerformed(e: ActionEvent): Unit = {
    aggregateManager.showEditor()
  }
}

class OpenHubNetClientEditor(workspace: AbstractWorkspaceScala, linkRoot: LinkRoot)
extends AbstractAction(loc"HubNet Client Editor")
with MenuAction {
  category = ToolsCategory
  group    = ToolsHubNetGroup
  mnemonic = 'B'

  override def actionPerformed(e: ActionEvent): Unit = {
    workspace.getHubNetManager.foreach { mgr =>
      mgr.openClientEditor()
      linkRoot.addLinkComponent(mgr.clientEditor)
    }
  }
}

class ConvertWidgetSizes(frame: AppFrame, widgetPanel: WidgetPanel)
  extends AbstractAction(loc"Convert Widgets to New Sizes") with MenuAction {

  category = ToolsCategory
  group    = ToolsWidgetGroup
  mnemonic = 'W'

  override def actionPerformed(e: ActionEvent): Unit = {
    val prompt = loc"""|This tool converts all widgets in the current model to the new larger sizes
                       |for NetLogo 7 and adjusts their positions to retain the original spacing between widgets.
                       |You will likely need to make further adjustments after using the tool."""

    if (new OptionPane(frame, loc"Convert Widgets to New Sizes", prompt,
                       Seq(loc"Resize and adjust positions", loc"Cancel"), OptionPane.Icons.info)
          .getSelectedIndex == 0) {
      widgetPanel.convertWidgetSizes()

      frame.smartPack(frame.getPreferredSize, false)
    }
  }
}
