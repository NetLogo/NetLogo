// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Color, Frame }
import javax.swing.{ BorderFactory, JButton, JDialog, JLabel, JPanel, JTabbedPane }

import org.nlogo.core.I18N
import org.nlogo.swing.{ ProgressListener, SwingWorker }

object LibrariesDialog {
  private val BottomPanelBorder = BorderFactory.createCompoundBorder(
    BorderFactory.createEmptyBorder(5, 0, 0, 0),
    BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(2, 0, 0, 0, Color.LIGHT_GRAY),
      BorderFactory.createEmptyBorder(10, 10, 10, 10)))
}

class LibrariesDialog(parent: Frame, categories: Map[String, LibrariesCategoryInstaller])
extends JDialog(parent, I18N.gui.get("tools.libraries"), false) {
  import LibrariesDialog._

  implicit val i18nPrefix = I18N.Prefix("tools.libraries")

  val tabs = new JTabbedPane
  val bottomPanel = new JPanel(new BorderLayout)
  val status = new JLabel
  val updateAllButton = new JButton(I18N.gui("updateAll"))

  val manager = new LibraryManager(categories, new ProgressListener {
    override def start()  = status.setText(I18N.gui("checkingForUpdates"))
    override def finish() = status.setText(null)
  })

  locally {
    manager.listModels.foreach { case (category, contents) =>
      tabs.addTab(I18N.gui("categories." + category),
        new LibrariesTab(contents,
          lib => {
            status.setText(I18N.gui("installing", lib.name))
            new Installer(category, lib).execute()
          },
          lib => {
            status.setText(I18N.gui("uninstalling", lib.name))
            new Uninstaller(category, lib).execute()
          }))
    }

    bottomPanel.setBorder(BottomPanelBorder)
    bottomPanel.add(status, BorderLayout.CENTER)
    bottomPanel.add(updateAllButton, BorderLayout.EAST)

    updateAllButton.addActionListener(_ => tabs.getSelectedComponent.asInstanceOf[LibrariesTab].updateAll())

    setLayout(new BorderLayout)
    add(tabs, BorderLayout.CENTER)
    add(bottomPanel, BorderLayout.SOUTH)
    setSize(550, 400)
  }

  override def setVisible(v: Boolean) = {
    super.setVisible(v)
    if (v) manager.updateMetadataFromGithub()
  }

  class Installer(category: String, lib: LibraryInfo) extends SwingWorker[Any, Any] {
    override def doInBackground() = manager.installer(category)(lib)
    override def onComplete() = {
      manager.reloadMetadata()
      status.setText(null)
    }
  }

  class Uninstaller(category: String, lib: LibraryInfo) extends SwingWorker[Any, Any] {
    override def doInBackground() = manager.uninstaller(category)(lib)
    override def onComplete() = {
      manager.reloadMetadata()
      status.setText(null)
    }
  }
}
