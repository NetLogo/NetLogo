package org.nlogo.app.tools

import java.awt.{ BorderLayout, Dimension, Font }
import java.awt.event.{ FocusEvent, TextEvent, TextListener }
import javax.swing.{ BorderFactory, DefaultComboBoxModel, ImageIcon, JButton,
  JComboBox, JPanel, JScrollPane }

import org.nlogo.core.I18N
import org.nlogo.api.PreviewCommands
import org.nlogo.api.PreviewCommands.{ Compilable, Custom, Default, Manual }
import org.nlogo.awt.Fonts.platformMonospacedFont
import org.nlogo.editor.EditorConfiguration
import org.nlogo.swing.HasPropertyChangeSupport
import org.nlogo.util.Implicits.RichString
import org.nlogo.window.{ CodeEditor, EditorAreaErrorLabel, EditorColorizer }

class EditorPanel(colorizer: EditorColorizer) extends JPanel {

  setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))

  val comboBox = new ComboBox
  val compileButton = new JButton(
    I18N.gui.get("tabs.code.checkButton"),
    new ImageIcon(classOf[EditorPanel].getResource("/images/check.gif"))
  )

  private var dirty = false
  val textListener = new TextListener with HasPropertyChangeSupport {
    override def textValueChanged(e: TextEvent): Unit = {
      dirty = true
      // forward the event (which is always null) to whoever is interested
      propertyChangeSupport.firePropertyChange("textValueChanged", null, null)
    }
  }
  val configuration =
    EditorConfiguration.default(0, 0, colorizer)
      .withFocusTraversalEnabled(true)
      .withListener(textListener)
  val editor = new CodeEditor(configuration) {
    override def getPreferredSize = new Dimension(350, 100)
    override def setText(text: String) = super.setText(text.stripTrailingWhiteSpace + "\n")
    override def getText = super.getText().stripTrailingWhiteSpace + "\n"
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
      case _         => getElementAt(customIndex)
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
