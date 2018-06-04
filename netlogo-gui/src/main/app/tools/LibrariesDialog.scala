// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Color, Frame }
import java.net.URL
import javax.swing.{ BorderFactory, JButton, JDialog, JLabel, JPanel, JTabbedPane, SwingWorker }

import org.nlogo.core.I18N
import org.nlogo.swing.ProgressListener

object LibrariesDialog {
  private val BottomPanelBorder = BorderFactory.createCompoundBorder(
    BorderFactory.createEmptyBorder(5, 0, 0, 0),
    BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(2, 0, 0, 0, Color.LIGHT_GRAY),
      BorderFactory.createEmptyBorder(10, 10, 10, 10)))
}

class LibrariesDialog(parent: Frame, categories: Map[String, (String, URL) => Unit])
extends JDialog(parent, I18N.gui.get("tools.libraries"), false) {
  import LibrariesDialog._

  implicit val i18nPrefix = I18N.Prefix("tools.libraries")

  val tabs = new JTabbedPane
  val bottomPanel = new JPanel(new BorderLayout)
  val checkForUpdates = new JButton(I18N.gui("checkForUpdates"))
  val status = new JLabel

  val manager = new LibraryManager(categories, new ProgressListener {
    override def start()  = status.setText(I18N.gui("checkingForUpdates"))
    override def finish() = status.setText(null)
  })

  locally {
    manager.listModels.foreach { case (category, contents) =>
      tabs.addTab(I18N.gui("categories." + category),
        new LibrariesTab(contents, (lib, url) => {
          status.setText(I18N.gui("installing", lib))
          new Installer(category, lib, url).execute()
        }))
    }

    bottomPanel.setBorder(BottomPanelBorder)
    bottomPanel.add(checkForUpdates, BorderLayout.EAST)
    bottomPanel.add(status, BorderLayout.CENTER)

    checkForUpdates.addActionListener(_ => manager.updateMetadataFromGithub())

    setLayout(new BorderLayout)
    add(tabs, BorderLayout.CENTER)
    add(bottomPanel, BorderLayout.SOUTH)
    setSize(500, 400)
  }

  class Installer(name: String, libName: String, url: URL) extends SwingWorker[Any, Any] {
    override def doInBackground() = manager.installer(name)(libName, url)
    override def done() = status.setText(null)
  }
}
