// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Color, FlowLayout, Frame }
import java.io.File
import java.nio.file.Path
import javax.swing.{ Action, BorderFactory, JButton, JLabel, JOptionPane, JPanel, JTabbedPane }

import org.nlogo.api.{ FileIO, LibraryInfoDownloader, LibraryManager }
import org.nlogo.core.I18N
import org.nlogo.swing.{ ProgressListener, SwingWorker }

class LibrariesDialog( parent:             Frame
                     , manager:            LibraryManager
                     , recompile:          () => Unit
                     , updateSource:       ((String) => String) => Unit
                     , getExtPathMappings: () => Map[String, Path]
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
  private lazy val buttonPanel     = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0))
  private lazy val libPathsButton  = new JButton(I18N.gui("showLibPaths"))
  private lazy val updateAllButton = new JButton

  protected override def initGUI(): Unit = {

    val category = "extensions"
    val tab      = new LibrariesTab("extensions", manager, status.setText, recompile, updateSource, getExtPathMappings)
    tabs.addTab(I18N.gui(s"categories.$category"), tab)

    bottomPanel.setBorder(bottomPanelBorder)
    bottomPanel.add(status, BorderLayout.CENTER)
    bottomPanel.add(buttonPanel, BorderLayout.EAST)

    // TODO: Once modules are added, this line must be removed -- EL 2018-08-13
    tabs.setLayout(new java.awt.CardLayout)

    tabs.addChangeListener(_ => updateAllButton.setAction(currentUpdateAllAction))

    libPathsButton.addActionListener {
      _ =>
        val mappingsStr = getExtPathMappings().map { case (k, v) => s"  * $k: $v" }.toSeq.sorted.mkString("\n")
        val msg = s"""${I18N.gui("libPathsExplanation")}
                     |
                     |$mappingsStr""".stripMargin
        JOptionPane.showMessageDialog(this, msg, "", JOptionPane.PLAIN_MESSAGE)
    }

    updateAllButton.setAction(currentUpdateAllAction)

    setLayout(new BorderLayout)
    add(tabs, BorderLayout.CENTER)
    add(bottomPanel, BorderLayout.SOUTH)
    setSize(650, 400)

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

      buttonPanel.getComponents.foreach(c => buttonPanel.remove(c))

      if (!getExtPathMappings().isEmpty)
        buttonPanel.add(libPathsButton)
      buttonPanel.add(updateAllButton)

    }
  }

}
