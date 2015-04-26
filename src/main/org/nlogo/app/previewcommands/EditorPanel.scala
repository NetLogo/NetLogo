package org.nlogo.app.previewcommands

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.FocusEvent
import java.awt.event.TextEvent
import java.awt.event.TextListener

import org.nlogo.api.I18N
import org.nlogo.api.PreviewCommands
import org.nlogo.api.PreviewCommands.Compilable
import org.nlogo.api.PreviewCommands.Custom
import org.nlogo.api.PreviewCommands.Default
import org.nlogo.api.PreviewCommands.Manual
import org.nlogo.app.CodeTab
import org.nlogo.awt.Fonts.platformMonospacedFont
import org.nlogo.swing.HasPropertyChangeSupport
import org.nlogo.window.CodeEditor
import org.nlogo.window.EditorAreaErrorLabel
import org.nlogo.window.EditorColorizer

import javax.swing.BorderFactory
import javax.swing.DefaultComboBoxModel
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JPanel
import javax.swing.JScrollPane

class EditorPanel(colorizer: EditorColorizer) extends JPanel {

  setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))

  val comboBox = new ComboBox
  val compileButton = new JButton(I18N.gui.get("tabs.code.checkButton"),
    new ImageIcon(classOf[CodeTab].getResource("/images/check.gif")))

  private var dirty = false
  val textListener = new TextListener with HasPropertyChangeSupport {
    override def textValueChanged(e: TextEvent): Unit = {
      dirty = true
      // forward the event (which is always null) to whoever is interested
      propertyChangeSupport.firePropertyChange("textValueChanged", null, null)
    }
  }
  val editorFont = new Font(platformMonospacedFont, Font.PLAIN, 12)
  val editor = new CodeEditor(0, 0, editorFont, false, textListener, colorizer, I18N.gui.get _) {
    override def getPreferredSize = new Dimension(350, 100)
    def stripTrailingWhiteSpace(s: String): String = s.lines
      .map(_.replaceAll("\\s+$", "")) // strips trailing whitespace at the end of each line
      .toSeq.reverse.dropWhile(_.isEmpty).reverse // drop empty lines at the end
      .mkString("\n") // and build the string (without a new line at the end)
    override def setText(text: String) = super.setText(stripTrailingWhiteSpace(text) + "\n")
    override def getText = stripTrailingWhiteSpace(super.getText)
    override def focusLost(fe: FocusEvent): Unit = {
      super.focusLost(fe)
      if (dirty) {
        dirty = false
        comboBox.updateCommands(PreviewCommands(getText))
      }
    }
  }
  def update(previewCommands: PreviewCommands): Unit = {
    editor.setText(previewCommands.source)
    editor.setEnabled(previewCommands.isInstanceOf[Compilable])
    dirty = false
  }
  val errorLabel = new EditorAreaErrorLabel(editor)

  object SourcePanel extends JPanel {
    setLayout(new BorderLayout)
    add(new JPanel {
      setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0))
      setLayout(new BorderLayout)
      add(errorLabel, BorderLayout.PAGE_START)
      add(new JScrollPane(editor), BorderLayout.CENTER)
    }, BorderLayout.CENTER)
  }
  setLayout(new BorderLayout)
  add(new JPanel {
    setLayout(new BorderLayout)
    add(comboBox, BorderLayout.CENTER)
    add(compileButton, BorderLayout.LINE_END)
  }, BorderLayout.PAGE_START)
  add(SourcePanel, BorderLayout.CENTER)
}

class PreviewCommandsComboBoxModel(val defaultCustomCommands: Custom = Custom(Default.source))
  extends DefaultComboBoxModel[PreviewCommands](Array[PreviewCommands](Default, defaultCustomCommands, Manual)) {
  val customIndex = getIndexOf(defaultCustomCommands)
  def update(currentCommands: PreviewCommands): Unit = {
    setSelectedItem(null)
    val custom = currentCommands match {
      case Custom(_) => currentCommands
      case _ => getElementAt(customIndex)
    }
    removeElementAt(customIndex)
    insertElementAt(custom, customIndex)
  }
}

class ComboBox(val model: PreviewCommandsComboBoxModel = new PreviewCommandsComboBoxModel)
  extends JComboBox[PreviewCommands](model) {
  def previewCommands: Option[PreviewCommands] = Option(getItemAt(getSelectedIndex))
  def updateCommands(newPreviewCommands: PreviewCommands): Unit = {
    model.update(newPreviewCommands)
    setSelectedItem(newPreviewCommands)
  }
}