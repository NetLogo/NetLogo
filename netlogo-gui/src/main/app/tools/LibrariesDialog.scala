// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Color, Frame }
import java.io.File
import javax.swing.{ Action, BorderFactory, JButton, JLabel, JPanel, JTabbedPane }

import org.nlogo.api.{ FileIO, LibraryInfoDownloader, LibraryManager }
import org.nlogo.core.I18N
import org.nlogo.swing.{ ProgressListener, SwingWorker }

class LibrariesDialog( parent: Frame, manager: LibraryManager
                     , recompile: () => Unit, updateSource: ((String) => String) => Unit
                     ) extends ToolDialog(parent, "libraries") {

  private lazy val bottomPanelBorder =
    BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(5, 0, 0, 0),
      BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(2, 0, 0, 0, Color.LIGHT_GRAY),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
      )
    )

  private lazy val tabs            = new JTabbedPane
  private lazy val bottomPanel     = new JPanel(new BorderLayout)
  private lazy val status          = new JLabel
  private lazy val updateAllButton = new JButton

  protected override def initGUI(): Unit = {

    val category = "extensions"
    val tab      = new LibrariesTab("extensions", manager, status.setText, recompile, updateSource)
    tabs.addTab(I18N.gui(s"categories.$category"), tab)

    bottomPanel.setBorder(bottomPanelBorder)
    bottomPanel.add(status, BorderLayout.CENTER)
    bottomPanel.add(updateAllButton, BorderLayout.EAST)

    // TODO: Once modules are added, this line must be removed -- EL 2018-08-13
    tabs.setLayout(new java.awt.CardLayout)

    tabs.addChangeListener(_ => updateAllButton.setAction(currentUpdateAllAction))

    updateAllButton.setAction(currentUpdateAllAction)

    setLayout(new BorderLayout)
    add(tabs, BorderLayout.CENTER)
    add(bottomPanel, BorderLayout.SOUTH)
    setSize(550, 400)

  }

  private def currentUpdateAllAction(): Action =
    tabs.getSelectedComponent.asInstanceOf[LibrariesTab].updateAllAction

  override def setVisible(isVisible: Boolean): Unit = {
    super.setVisible(isVisible)
    if (isVisible) {

      val listener =
        new ProgressListener {
          override def start()  = status.setText(I18N.gui("checkingForUpdates"))
          override def finish() = status.setText(null)
        }

      listener.start()

      (new SwingWorker[Any, Any] {

        private var changed = false

        override def doInBackground(): Unit = {
          LibraryInfoDownloader(manager.metadataURL, (_: File) => { changed = true })
        }

        override def onComplete(): Unit = {
          if (changed) {
            val hash = LibraryInfoDownloader.urlToHash(manager.metadataURL)
            manager.updateLists(new File(FileIO.perUserFile(hash)))
          }
          listener.finish()
        }

      }).execute()

    }
  }

}
