// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.Frame
import java.nio.file.Path
import javax.swing.JLabel

import scala.concurrent.ExecutionContext

import org.nlogo.api.LibraryManager
import org.nlogo.core.{ I18N, LibraryInfo, Token }
import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, CustomOptionPane, DialogButton, OptionPane, ProgressListener,
                         ScrollPane, TextArea, Utils, WindowAutomator, Zoomable, ZoomableBorder, ZoomActions }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class LibrariesDialog( parent:          Frame
                     , manager:         LibraryManager
                     , recompile:       () => Unit
                     , tokenizeSource:  String => Iterator[Token]
                     , updateSource:    ((String) => String) => Unit
                     , extPathMappings: Map[String, Path]
                     ) extends ToolDialog(parent, "libraries") with ZoomActions with ThemeSync {

  WindowAutomator.automate(this)

  private lazy val tab             = new LibrariesTab("extensions", manager, status.setText, recompile, tokenizeSource,
                                                      updateSource, extPathMappings)
  private lazy val status          = new JLabel
  private lazy val libPathsButton  = new DialogButton(false, I18N.gui("showLibPaths"), () => {
    val mappingsStr = extPathMappings.map { case (k, v) => s"  * $k: $v" }.toSeq.sorted.mkString("\n")
    val msg = s"""${I18N.gui("libPathsExplanation")}
                  |
                  |$mappingsStr""".stripMargin
    val textArea = new TextArea(15, 60, msg) {
      setLineWrap(true)
      setWrapStyleWord(true)
      setEditable(false)
    }
    val scrollPane = new ScrollPane(textArea) {
      setBackground(InterfaceColors.textAreaBackground())
    }
    new CustomOptionPane(LibrariesDialog.this, I18N.gui("showLibPaths"), scrollPane, OptionPane.Options.Ok)
  }: Unit)
  private lazy val updateAllButton = new DialogButton(true, tab.updateAllAction)

  private lazy val content = new BoxColumn(Seq(
    tab,
    new BoxRow(Seq(
      status,
      libPathsButton,
      updateAllButton
    ), 6, BoxAlign.End)
  ), 10) with Zoomable {
    setBorder(new ZoomableBorder(10, 10, 10, 10))

    override def zoomComponent(): Unit = {
      resetSize()
    }
  }

  protected override def initGUI(): Unit = {
    add(content)

    resetSize()
  }

  override def setVisible(isVisible: Boolean): Unit = {
    if (isVisible) {

      val listener =
        new ProgressListener {
          override def start()  = status.setText(I18N.gui("checkingForUpdates"))
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

  private def resetSize(): Unit = {
    setSize(Utils.zoom(650), Utils.zoom(400))
  }

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground())

    tab.syncTheme()

    libPathsButton.syncTheme()
    updateAllButton.syncTheme()

    status.setForeground(InterfaceColors.dialogText())
  }

  private [app] def searchFor(text: String, expectedSize: Int): Option[Seq[LibraryInfo]] =
    tab.searchFor(text, expectedSize)

  private [app] def testInstall(info: LibraryInfo): Unit = {
    tab.testInstall(info)
  }
}
