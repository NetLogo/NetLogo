// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Frame
import java.awt.event.ActionEvent
import java.nio.file.Path
import javax.swing.{ AbstractAction, JDialog }

import org.nlogo.api.{ AggregateManagerInterface, LibraryManager }
import org.nlogo.app.common.TabsInterface
import org.nlogo.app.interfacetab.WidgetPanel
import org.nlogo.app.tools.{ LibrariesDialog, Preferences, PreferencesDialog, ThemesDialog }
import org.nlogo.awt.Positioning
import org.nlogo.core.I18N
import org.nlogo.shape.ShapesManagerInterface
import org.nlogo.swing.{ OptionPane, UserAction }, UserAction._
import org.nlogo.theme.ThemeSync
import org.nlogo.window.{ ColorDialog, LinkRoot, RGBAColorDialog }
import org.nlogo.workspace.AbstractWorkspaceScala

abstract class ShowDialogAction(name: String) extends AbstractAction(name) with ThemeSync {
  protected def createDialog(): JDialog with ThemeSync

  lazy protected val createdDialog = createDialog

  override def actionPerformed(e: ActionEvent): Unit = {
    createdDialog.toFront()
    createdDialog.setVisible(true)
  }

  override def syncTheme(): Unit = {
    createdDialog.syncTheme()
  }
}

class ShowPreferencesDialog(frame: Frame, tabs: TabsInterface)
extends ShowDialogAction(I18N.gui.get("menu.tools.preferences"))
with MenuAction {
  category = ToolsCategory
  group    = ToolsSettingsGroup

  override def createDialog = new PreferencesDialog(frame,
    Seq(
      Preferences.Language,
      Preferences.LoadLastOnStartup,
      new Preferences.ReloadOnExternalChanges(tabs),
      Preferences.IsLoggingEnabled,
      new Preferences.LogDirectory(frame),
      Preferences.LogEvents,
      Preferences.IncludedFilesMenu,
      Preferences.ProceduresMenuSortOrder,
      Preferences.FocusOnError,
      Preferences.StartSeparateCodeTab
    ) ++ (if (System.getProperty("os.name").contains("Linux")) Seq(Preferences.UIScale) else Nil)
  )
}

class ShowThemesDialog(frame: Frame with ThemeSync)
  extends ShowDialogAction(I18N.gui.get("menu.tools.themes")) with MenuAction {

  category = ToolsCategory
  group = ToolsSettingsGroup

  override def createDialog = new ThemesDialog(frame)
}

class OpenLibrariesDialog( frame:              Frame
                         , libManager:         LibraryManager
                         , recompile:          () => Unit
                         , updateSource:       ((String) => String) => Unit
                         , getExtPathMappings: () => Map[String, Path]
                         ) extends ShowDialogAction(I18N.gui.get("menu.tools.extensions")) with MenuAction {

  category = ToolsCategory
  group    = ToolsSettingsGroup

  def createDialog() = new LibrariesDialog(frame, libManager, recompile, updateSource, getExtPathMappings())

}

class OpenColorDialog(frame: Frame)
extends ShowDialogAction(I18N.gui.get("menu.tools.colorSwatches"))
with MenuAction {
  category = ToolsCategory
  group = ToolsDialogsGroup

  def createDialog() = new ColorDialog(frame, false)

  var hasShown = false

  override def actionPerformed(e: ActionEvent) {
    Positioning.center(createdDialog, frame)
    if (!hasShown) {
      createdDialog.asInstanceOf[ColorDialog].showDialog()
      hasShown = true
    } else
      super.actionPerformed(e)
  }
}

class OpenRGBAColorDialog(frame: Frame) extends ShowDialogAction(I18N.gui.get("menu.tools.rgbaColorPicker"))
                                        with MenuAction {
  category = ToolsCategory
  group = ToolsDialogsGroup

  def createDialog() = new RGBAColorDialog(frame, false)

  override def actionPerformed(e: ActionEvent) {
    Positioning.center(createdDialog, frame)

    super.actionPerformed(e)
  }
}

class ShowShapeManager(key: String, shapeManager: => ShapesManagerInterface)
extends AbstractAction(I18N.gui.get(s"menu.tools.$key"))
with MenuAction {
  category = ToolsCategory
  group    = ToolsDialogsGroup

  override def actionPerformed(e: ActionEvent) {
    shapeManager.init(I18N.gui.get(s"menu.tools.$key"))
  }
}

class ShowSystemDynamicsModeler(aggregateManager: AggregateManagerInterface)
extends AbstractAction(I18N.gui.get("menu.tools.systemDynamicsModeler"))
with MenuAction {
  category    = ToolsCategory
  group       = ToolsDialogsGroup
  accelerator = KeyBindings.keystroke('D', withMenu = true, withShift = true)

  override def actionPerformed(e: ActionEvent) {
    aggregateManager.showEditor()
  }
}

class OpenHubNetClientEditor(workspace: AbstractWorkspaceScala, linkRoot: LinkRoot)
extends AbstractAction(I18N.gui.get("menu.tools.hubNetClientEditor"))
with MenuAction {
  category = ToolsCategory
  group    = ToolsHubNetGroup

  override def actionPerformed(e: ActionEvent) {
    workspace.getHubNetManager.foreach { mgr =>
      mgr.openClientEditor()
      linkRoot.addLinkComponent(mgr.clientEditor)
    }
  }
}

class ConvertWidgetSizes(frame: Frame, widgetPanel: WidgetPanel)
  extends AbstractAction(I18N.gui.get("menu.tools.convertWidgetSizes")) with MenuAction {

  category = ToolsCategory
  group    = ToolsWidgetGroup

  override def actionPerformed(e: ActionEvent): Unit = {
    if (new OptionPane(frame, I18N.gui.get("common.messages.warning"),
                       I18N.gui.get("menu.tools.convertWidgetSizes.prompt"), OptionPane.Options.OkCancel,
                       OptionPane.Icons.Warning).getSelectedIndex == 0) {
      widgetPanel.convertWidgetSizes()
    }
  }
}
