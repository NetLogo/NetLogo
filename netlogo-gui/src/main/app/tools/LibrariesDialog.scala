// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Color, Frame }
import java.net.URL
import javax.swing.{ BorderFactory, JButton, JDialog, JPanel, JTabbedPane }

import org.nlogo.core.I18N

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

  val manager = new LibraryManager(categories)

  locally {
    val tabs = new JTabbedPane
    manager.listModels.foreach { case (name, contents) =>
      tabs.addTab(I18N.gui.get("tools.libraries.categories." + name), new LibrariesTab(contents, manager.installer(name)))
    }

    val bottomPanel = new JPanel(new BorderLayout)
    val checkForUpdates = new JButton(I18N.gui.get("tools.libraries.checkForUpdates"))
    bottomPanel.setBorder(BottomPanelBorder)
    bottomPanel.add(checkForUpdates, BorderLayout.EAST)

    checkForUpdates.addActionListener(_ => manager.updateMetadataFromGithub())

    setLayout(new BorderLayout)
    add(tabs, BorderLayout.CENTER)
    add(bottomPanel, BorderLayout.SOUTH)
    setSize(500, 300)
  }
}
