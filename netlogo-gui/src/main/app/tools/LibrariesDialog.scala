// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Color, Frame }
import javax.swing.{ BorderFactory, JButton, JLabel, JPanel, JTabbedPane }

import org.nlogo.core.I18N
import org.nlogo.swing.ProgressListener

object LibrariesDialog {
  private val BottomPanelBorder = BorderFactory.createCompoundBorder(
    BorderFactory.createEmptyBorder(5, 0, 0, 0),
    BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(2, 0, 0, 0, Color.LIGHT_GRAY),
      BorderFactory.createEmptyBorder(10, 10, 10, 10)))
}

class LibrariesDialog(parent: Frame, categories: Map[String, LibrariesCategoryInstaller])
extends ToolDialog(parent, "libraries") {
  import LibrariesDialog._

  private lazy val tabs = new JTabbedPane
  private lazy val bottomPanel = new JPanel(new BorderLayout)
  private lazy val status = new JLabel
  private lazy val updateAllButton = new JButton

  private lazy val manager = new LibraryManager(categories, new ProgressListener {
    override def start()  = status.setText(I18N.gui("checkingForUpdates"))
    override def finish() = status.setText(null)
  })

  protected override def initGUI() = {
    manager.listModels.foreach { case (category, contents) =>
      tabs.addTab(I18N.gui("categories." + category),
        new LibrariesTab(category, contents,
          manager.installer(category), manager.uninstaller(category),
          status.setText, manager.reloadMetadata _))
    }

    bottomPanel.setBorder(BottomPanelBorder)
    bottomPanel.add(status, BorderLayout.CENTER)
    bottomPanel.add(updateAllButton, BorderLayout.EAST)

    tabs.addChangeListener(_ => updateAllButton.setAction(currentUpdateAllAction))

    updateAllButton.setAction(currentUpdateAllAction)

    setLayout(new BorderLayout)
    add(tabs, BorderLayout.CENTER)
    add(bottomPanel, BorderLayout.SOUTH)
    setSize(550, 400)
  }

  private def currentUpdateAllAction =
    tabs.getSelectedComponent.asInstanceOf[LibrariesTab].updateAllAction

  override def setVisible(v: Boolean) = {
    super.setVisible(v)
    if (v) manager.updateMetadataFromGithub()
  }
}
