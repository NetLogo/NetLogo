// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ BorderLayout, Component, FileDialog => JFileDialog }
import java.io.File
import java.nio.file.Path
import javax.swing.{ JLabel, JToolBar }

import org.nlogo.awt.UserCancelException
import org.nlogo.swing.Implicits.thunk2documentListener
import org.nlogo.swing.{ Button, FileDialog, TextField, Transparent }
import org.nlogo.theme.InterfaceColors

class FilePathEditor(accessor: PropertyAccessor[String], parent: Component, currentDirectory: Option[Path],
                     suggestedFile: Option[String]) extends PropertyEditor(accessor) {

  private val suggestedFileName: String = suggestedFile.map(_.trim).filter(_.nonEmpty).getOrElse(s"${accessor.name}-export.csv")
  private val homePath: Path = (new File(System.getProperty("user.home"))).toPath.toAbsolutePath

  private val label = new JLabel(accessor.name)
  private val editor = new TextField(12) {
    getDocument.addDocumentListener(() => accessor.changed())
  }

  private val browseButton = new Button("Browse...", () => {
    try {
      this.set(FileDialog.showFiles(parent, s"${accessor.name} export", JFileDialog.SAVE,
                                    asPath(getCurrentText).getFileName.toString).trim)
    } catch {
      case ex: UserCancelException =>
    }
  })

  private val disableButton = new Button("Disable", () => set(""))

  private val toolbar = new JToolBar with Transparent {
    setLayout(new BorderLayout(6, 0))
    setFloatable(false)

    add(browseButton, BorderLayout.WEST)
    add(disableButton, BorderLayout.CENTER)
  }

  setLayout(new BorderLayout(6, 0))

  add(label, BorderLayout.WEST)
  add(editor, BorderLayout.CENTER)
  add(toolbar, BorderLayout.EAST)

  private def asPath(currentText: String): Path = {
    val currentPath = new File(currentText).toPath
    val path = if (currentPath.isAbsolute) {
      currentPath
    } else {
      currentDirectory.getOrElse(homePath).resolve(currentPath)
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

  override def requestFocus(): Unit = { editor.requestFocus() }

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.dialogText())

    editor.syncTheme()
    browseButton.syncTheme()
    disableButton.syncTheme()
  }
}
