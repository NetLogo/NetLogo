// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Component, FileDialog => JFileDialog }
import java.io.File
import java.nio.file.Path
import javax.swing.{ JLabel, JToolBar }

import org.nlogo.awt.UserCancelException
import org.nlogo.swing.Implicits.thunk2documentListener
import org.nlogo.swing.{ Button, FileDialog, TextField, Transparent }
import org.nlogo.theme.InterfaceColors

class FilePathEditor(accessor: PropertyAccessor[String], parent: Component, suggestedFile: Option[String])
  extends PropertyEditor(accessor) {

  private val suggestedFileName: String = suggestedFile.map(_.trim).filter(_.nonEmpty).getOrElse(s"${accessor.name}-export.csv")
  private val homePath: Path = (new File(System.getProperty("user.home"))).toPath.toAbsolutePath

  private val label = new JLabel(accessor.name)
  private val editor = new TextField(12) {
    getDocument.addDocumentListener(() => accessor.changed())
  }

  private val browseButton = new Button("Browse...", () => {
    try {
      val filePath = asPath(getCurrentText)
      FileDialog.setDirectory(filePath.getParent.toString)
      val outName = FileDialog.showFiles(parent, s"${accessor.name} export", JFileDialog.SAVE,
                                         filePath.getFileName.toString)
      this.set(outName.trim)
    } catch {
      case ex: UserCancelException =>
    }
  })

  private val disableButton = new Button("Disable", () => set(""))

  private val toolbar = new JToolBar with Transparent {
    setFloatable(false)

    add(browseButton)
    add(disableButton)
  }

  add(label)
  add(editor)
  add(toolbar)

  private def asPath(currentText: String): Path = {
    val currentPath = new File(currentText).toPath
    val path = if (currentPath.isAbsolute) {
      currentPath
    } else {
      homePath.resolve(currentPath)
    }
    if (path.toFile.isDirectory) {
      path.resolve(suggestedFileName)
    } else {
      path
    }
  }

  private def getCurrentText: String = {
    Option(editor.getText) match {
      case Some(text) => text.trim
      case None => ""
    }
  }

  override def get: Option[String] = {
    val currentText = getCurrentText
    if (currentText == "") {
      Option(currentText)
    } else {
      Option(asPath(currentText).toString)
    }
  }

  override def set(value: String): Unit = { editor.setText(value) }

  override def requestFocus() { editor.requestFocus() }

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.dialogText())

    editor.syncTheme()
    browseButton.syncTheme()
    disableButton.syncTheme()
  }
}
