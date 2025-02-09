// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ BorderLayout, Component, FileDialog => JFileDialog, GridBagConstraints }
import java.io.File
import javax.swing.{ JLabel, JToolBar }

import org.nlogo.awt.UserCancelException
import org.nlogo.swing.Implicits._
import org.nlogo.swing.{ Button, FileDialog, TextField, Transparent }
import org.nlogo.theme.InterfaceColors

abstract class FilePathEditor(accessor: PropertyAccessor[String], parent: Component, suggestedFile: String)
  extends PropertyEditor(accessor) {

  val suggestedFileName = if (suggestedFile != null && suggestedFile.trim() != "") {
    suggestedFile
  } else {
    s"${accessor.displayName}-export.csv"
  }
  val homePath = (new File(System.getProperty("user.home"))).toPath.toAbsolutePath

  private val editor = new TextField(12)
  setLayout(new BorderLayout(BORDER_PADDING, 0))
  private val label = new JLabel(accessor.displayName)
  add(label, BorderLayout.WEST)
  editor.getDocument().addDocumentListener({ () => changed() })
  add(editor, BorderLayout.CENTER)
  private val toolbar = new JToolBar with Transparent
  toolbar.setFloatable(false)
  private val browseButton = new Button("Browse...", () => {
    try {
      val currentText = getCurrentText()
      val filePath    = asPath(currentText)
      FileDialog.setDirectory(filePath.getParent.toString)
      val outName = FileDialog.showFiles(parent, s"${accessor.displayName} export", JFileDialog.SAVE, filePath.getFileName.toString)
      this.set(outName.trim())
    } catch {
      case ex: UserCancelException =>
        println(s"User canceled the ${accessor.displayName} file browser.")
    }
  })
  toolbar.add(browseButton)
  private val disableButton = new Button("Disable", () => {
    set("")
  })
  toolbar.add(disableButton)
  add(toolbar, BorderLayout.EAST)

  private def asPath(currentText: String) = {
    val currentFile = new File(currentText)
    val currentPath = currentFile.toPath
    val path = if (currentPath.isAbsolute) {
      currentPath
    } else {
      homePath.resolve(currentPath)
    }
    if (path.toFile.isDirectory) {
      val suggestedPath = path.resolve(suggestedFileName)
      suggestedPath
    } else {
      path
    }
  }

  private def getCurrentText() = {
    val currentTextMaybe = editor.getText
    val currentText      = if (currentTextMaybe == null) { "" } else { currentTextMaybe.trim }
    currentText
  }

  override def get = {
    val currentText = getCurrentText()
    if (currentText == "") {
      Option(currentText)
    } else {
      val filePath   = asPath(currentText)
      val pathString = filePath.toString
      Option(pathString)
    }
  }

  override def set(value: String) { editor.setText(value) }

  override def requestFocus() { editor.requestFocus() }

  override def getConstraints = {
    val c = super.getConstraints
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 0.25
    c
  }

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.DIALOG_TEXT)

    editor.syncTheme()
    browseButton.syncTheme()
    disableButton.syncTheme()
  }
}
