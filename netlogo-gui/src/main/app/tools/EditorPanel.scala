// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ Dimension, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ FocusEvent, TextEvent, TextListener }
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

import org.nlogo.api.{ CompilerServices, PreviewCommands }, PreviewCommands.{ Compilable, Custom, Default, Manual }
import org.nlogo.core.I18N
import org.nlogo.editor.{ EditorArea, EditorConfiguration }
import org.nlogo.swing.{ Button, ComboBox, HasPropertyChangeSupport, ScrollPane, Transparent, Utils }
import org.nlogo.theme.InterfaceColors
import org.nlogo.util.Implicits.RichString
import org.nlogo.window.{ EditorAreaErrorLabel, EditorColorizer }

class EditorPanel(compiler: CompilerServices, colorizer: EditorColorizer)
  extends JPanel(new GridBagLayout) with Transparent {

  val comboBox = new PreviewCommandsComboBox
  val compileButton = new Button("", () => {
    if (dirty) {
      dirty = false
      updateCompileIcon()
      comboBox.updateCommands(PreviewCommands(editor.getText()))
    }
  }) {
    setBorder(new EmptyBorder(4, 4, 4, 4))
  }

  private var dirty = false
  val textListener = new TextListener with HasPropertyChangeSupport {
    override def textValueChanged(e: TextEvent): Unit = {
      dirty = true
      updateCompileIcon()
      // forward the event (which is always null) to whoever is interested
      propertyChangeSupport.firePropertyChange("textValueChanged", null, null)
    }
  }
  val configuration =
    EditorConfiguration.default(0, 0, compiler, colorizer)
      .withFocusTraversalEnabled(true)
      .withListener(textListener)
  val editor = new EditorArea(configuration) {
    setBackground(InterfaceColors.codeBackground())
    setCaretColor(InterfaceColors.textAreaText())

    override def getPreferredSize = new Dimension(350, 100)
    override def setText(text: String) = super.setText(text.stripTrailingWhiteSpace + "\n")
    override def getText = super.getText().stripTrailingWhiteSpace + "\n"
    override def focusLost(fe: FocusEvent): Unit = {
      super.focusLost(fe)
      if (dirty) {
        dirty = false
        updateCompileIcon()
        comboBox.updateCommands(PreviewCommands(getText))
      }
    }
  }

  val errorLabel = new EditorAreaErrorLabel(editor)

  updateCompileIcon()

  locally {
    val c = new GridBagConstraints

    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(comboBox, c)

    c.fill = GridBagConstraints.NONE
    c.weightx = 0
    c.insets = new Insets(6, 0, 6, 6)

    add(compileButton, c)

    c.gridy = 1
    c.gridwidth = 2
    c.fill = GridBagConstraints.HORIZONTAL
    c.insets = new Insets(0, 6, 0, 6)

    add(errorLabel, c)

    c.gridy = 2
    c.fill = GridBagConstraints.BOTH
    c.weighty = 1

    add(new ScrollPane(editor) {
      setBackground(InterfaceColors.codeBackground())
    }, c)
  }

  def update(previewCommands: PreviewCommands): Unit = {
    editor.setText(previewCommands.source)
    editor.setEnabled(previewCommands.isInstanceOf[Compilable])
    dirty = false
    updateCompileIcon()
  }

  private def updateCompileIcon(): Unit = {
    compileButton.setIcon(Utils.iconScaledWithColor("/images/check.png", 15, 15,
                                                    if (dirty) {
                                                      InterfaceColors.checkFilled()
                                                    } else {
                                                      InterfaceColors.toolbarImage()
                                                    }))
  }
}

// the PreviewCommands trait is in netlogo-core, so it doesn't have access to the necessary I18N stuff (Isaac B 7/6/25)
case class PreviewCommandsWrapper(commands: PreviewCommands) {
  override def toString: String = {
    commands match {
      case Manual => I18N.gui.get("tools.previewCommands.manual")
      case Default => I18N.gui.get("tools.previewCommands.default")
      case _: Custom => I18N.gui.get("tools.previewCommands.custom")
      case c => c.toString
    }
  }
}

class PreviewCommandsComboBox extends ComboBox[PreviewCommandsWrapper](
  List(Default, Custom(Default.source), Manual).map(PreviewCommandsWrapper(_))) {

  def updateCommands(newPreviewCommands: PreviewCommands): Unit = {
    if (newPreviewCommands.isInstanceOf[Custom])
      setItems(List(Default, newPreviewCommands, Manual).map(PreviewCommandsWrapper(_)))

    setSelectedItem(PreviewCommandsWrapper(newPreviewCommands))
  }
}
