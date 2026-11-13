// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ Dimension, Frame }
import java.nio.file.Path
import javax.swing.JLabel

import scala.concurrent.ExecutionContext

import org.nlogo.api.LibraryManager
import org.nlogo.core.{ LibraryInfo, LocaleContext, Token }
import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, CustomOptionPane, DialogButton, OptionPane, ProgressListener,
                         ScrollPane, TextArea, WindowAutomator, ZoomableBorder }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class LibrariesDialog( parent:          Frame
                     , manager:         LibraryManager
                     , recompile:       () => Unit
                     , tokenizeSource:  String => Iterator[Token]
                     , updateSource:    ((String) => String) => Unit
                     , extPathMappings: Map[String, Path]
                     ) extends ToolDialog(parent, loc"Extensions") with ThemeSync {

  WindowAutomator.automate(this)

  private lazy val tab             = new LibrariesTab("extensions", manager, status.setText, recompile, tokenizeSource,
                                                      updateSource, extPathMappings)
  private lazy val status          = new JLabel
  private lazy val libPathsButton  = new DialogButton(false, loc"View Conflicting Libraries", () => {
    val mappingsStr = extPathMappings.map { case (k, v) => s"  * $k: $v" }.toSeq.sorted.mkString("\n")
    val msg = loc"""|The below extensions will override any extension with the same name that is installed through the
                    |Extension Manager.  If you wish to use a version of the extension downloaded through the Extension
                    |Manager instead, you must first remove the files at the specified location.\n\nSee the "Extensions
                    |Guide" and the "Extension Manager" pages of the NetLogo User Manual for more information.\n\n
                    |$mappingsStr"""
    val textArea = new TextArea(15, 60, msg) {
      setLineWrap(true)
      setWrapStyleWord(true)
      setEditable(false)
    }
    val scrollPane = new ScrollPane(textArea) {
      setBackground(InterfaceColors.textAreaBackground())
    }
    new CustomOptionPane(LibrariesDialog.this, loc"View Conflicting Libraries", scrollPane, OptionPane.Options.Ok)
  }: Unit)
  private lazy val updateAllButton = new DialogButton(true, tab.updateAllAction)

  add(new BoxColumn(Seq(
    tab,
    new BoxRow(Seq(
      status,
      libPathsButton,
      updateAllButton
    ), 6, BoxAlign.End)
  ), 10) {
    setBorder(new ZoomableBorder(10, 10, 10, 10))
  })

  pack()

  override def setVisible(isVisible: Boolean): Unit = {
    if (isVisible) {

      val listener =
        new ProgressListener {
          override def start()  = status.setText(loc"Checking for updates...")
          override def finish() = status.setText(null)
        }

      listener.start()

      import ExecutionContext.Implicits.global
      manager.updateMetadata().foreach {
        _ => listener.finish()
      }

      libPathsButton.setVisible(extPathMappings.nonEmpty)

    }

    super.setVisible(isVisible)
  }

  override def getPreferredSize: Dimension =
    new Dimension(zoom(650), zoom(400))

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground())

    status.setForeground(InterfaceColors.dialogText())

    tab.syncTheme()
    libPathsButton.syncTheme()
    updateAllButton.syncTheme()
  }

  private [app] def searchFor(text: String, expectedSize: Int): Option[Seq[LibraryInfo]] =
    tab.searchFor(text, expectedSize)

  private [app] def testInstall(info: LibraryInfo): Unit = {
    tab.testInstall(info)
  }
}
