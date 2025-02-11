// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, FlowLayout, Frame }
import java.io.File
import java.nio.file.Path
import javax.swing.{ JLabel, JPanel }
import javax.swing.border.EmptyBorder

import org.nlogo.api.{ FileIO, LibraryInfoDownloader, LibraryManager }
import org.nlogo.core.I18N
import org.nlogo.swing.{ Button, ProgressListener, OptionPane, SwingWorker, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class LibrariesDialog( parent:          Frame
                     , manager:         LibraryManager
                     , recompile:       () => Unit
                     , updateSource:    ((String) => String) => Unit
                     , extPathMappings: Map[String, Path]
                     ) extends ToolDialog(parent, "libraries") with ThemeSync {

  // `tabs` can be converted back to a `JTabbedPane` once other libraries are added, like code modules or models.
  // -JeremyB April 2019
  private lazy val tabs            = new JPanel(new BorderLayout)
  private lazy val tab             = new LibrariesTab("extensions", manager, status.setText, recompile, updateSource, extPathMappings)
  private lazy val bottomPanel     = new JPanel(new BorderLayout)
  private lazy val status          = new JLabel
  private lazy val buttonPanel     = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) with Transparent
  private lazy val libPathsButton  = new Button(I18N.gui("showLibPaths"), () => {
    val mappingsStr = extPathMappings.map { case (k, v) => s"  * $k: $v" }.toSeq.sorted.mkString("\n")
    val msg = s"""${I18N.gui("libPathsExplanation")}
                  |
                  |$mappingsStr""".stripMargin
    new OptionPane(LibrariesDialog.this, I18N.gui("showLibPaths"), msg, OptionPane.Options.Ok)
  })
  private lazy val updateAllButton = new Button(tab.updateAllAction)

  protected override def initGUI(): Unit = {
    tabs.add(tab)

    bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10))
    bottomPanel.add(status, BorderLayout.CENTER)
    bottomPanel.add(buttonPanel, BorderLayout.EAST)

    setLayout(new BorderLayout)
    add(tabs, BorderLayout.CENTER)
    add(bottomPanel, BorderLayout.SOUTH)
    setSize(650, 400)
  }

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

  override def syncTheme(): Unit = {
    tab.syncTheme()

    bottomPanel.setBackground(InterfaceColors.dialogBackground)

    libPathsButton.syncTheme()
    updateAllButton.syncTheme()

    status.setForeground(InterfaceColors.dialogText)
  }
}
