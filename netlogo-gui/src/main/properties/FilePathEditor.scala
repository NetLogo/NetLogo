// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ BorderLayout, Component, FileDialog => JFileDialog, GridBagConstraints }
import javax.swing.{ JLabel, JToolBar }

import org.nlogo.awt.{ UserCancelException }
import org.nlogo.swing.Implicits._
import org.nlogo.swing.{ FileDialog, RichJButton, TextField }

abstract class FilePathEditor(accessor: PropertyAccessor[String], parent: Component, suggestedFile: String)
  extends PropertyEditor(accessor)
{
  val editor = makeEditor()
  setLayout(new BorderLayout(BORDER_PADDING, 0))
  add(new JLabel(accessor.displayName), BorderLayout.WEST)
  editor.getDocument().addDocumentListener({ () => changed() })
  add(editor, BorderLayout.CENTER)
  val toolbar = new JToolBar()
  toolbar.setFloatable(false)
  val browseButton = RichJButton("Browse...") {
    try {
      val suggest = if (suggestedFile == null || suggestedFile.trim() == "") {
        s"${accessor.displayName}-export.csv"
      } else {
        suggestedFile
      }
      val fileName = FileDialog.showFiles(parent, s"${accessor.displayName} export", JFileDialog.SAVE, suggest)
      this.set(fileName.trim())
    } catch {
      case ex: UserCancelException => println("User canceled file browser.")
    }
  }
  toolbar.add(browseButton)
  val disableButton = RichJButton("Disable") { this.set("") }
  toolbar.add(disableButton)
  add(toolbar, BorderLayout.EAST)

  def makeEditor() = new TextField(12)

  override def get = Option(editor.getText)

  override def set(value: String) { editor.setText(value) }

  override def requestFocus() { editor.requestFocus() }

  override def getConstraints = {
    val c = super.getConstraints
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 0.25
    c
  }
}
