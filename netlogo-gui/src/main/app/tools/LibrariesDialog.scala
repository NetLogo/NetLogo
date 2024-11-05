// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Color, FlowLayout, Frame }
import java.io.File
import java.nio.file.Path
import javax.swing.{ Action, BorderFactory, JButton, JLabel, JOptionPane, JPanel }

import org.nlogo.api.{ FileIO, LibraryInfoDownloader, LibraryManager }
import org.nlogo.core.I18N
import org.nlogo.swing.{ ProgressListener, SwingWorker }
import org.nlogo.theme.ThemeSync

class LibrariesDialog( parent:          Frame
                     , manager:         LibraryManager
                     , recompile:       () => Unit
                     , updateSource:    ((String) => String) => Unit
                     , extPathMappings: Map[String, Path]
                     ) extends ToolDialog(parent, "libraries") with ThemeSync {

  private lazy val bottomPanelBorder =
    BorderFactory.createCompoundBorder(
      BorderFactory.createEmptyBorder(5, 0, 0, 0),
      BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(2, 0, 0, 0, Color.LIGHT_GRAY),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
      )
    )

  // `tabs` can be converted back to a `JTabbedPane` once other libraries are added, like code modules or models.
  // -JeremyB April 2019
  private lazy val tabs            = new JPanel(new BorderLayout)
  private lazy val bottomPanel     = new JPanel(new BorderLayout)
  private lazy val status          = new JLabel
  private lazy val buttonPanel     = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0))
  private lazy val libPathsButton  = new JButton(I18N.gui("showLibPaths"))
  private lazy val updateAllButton = new JButton
  private lazy val tab             = new LibrariesTab("extensions", manager, status.setText, recompile, updateSource, extPathMappings)

  protected override def initGUI(): Unit = {

    tabs.add(tab)

    bottomPanel.setBorder(bottomPanelBorder)
    bottomPanel.add(status, BorderLayout.CENTER)
    bottomPanel.add(buttonPanel, BorderLayout.EAST)

    libPathsButton.addActionListener {
      _ =>
        val mappingsStr = extPathMappings.map { case (k, v) => s"  * $k: $v" }.toSeq.sorted.mkString("\n")
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
    tab.updateAllAction

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

      if (!extPathMappings.isEmpty)
        buttonPanel.add(libPathsButton)
      buttonPanel.add(updateAllButton)

    }
  }

  def syncTheme() {

  }
}
