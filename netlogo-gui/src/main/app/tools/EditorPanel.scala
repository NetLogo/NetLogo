// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.Dimension
import java.awt.event.{ FocusEvent, TextEvent, TextListener }
import javax.swing.{ BoxLayout, JPanel }

import org.nlogo.api.{ CompilerServices, PreviewCommands }, PreviewCommands.{ Compilable, Custom, Default, Manual }
import org.nlogo.core.I18N
import org.nlogo.editor.{ EditorArea, EditorConfiguration }
import org.nlogo.swing.{ BoxRow, Button, ComboBox, HasPropertyChangeSupport, HorizontalStrut, PreferredSize, ScrollPane,
                         Transparent, Utils, VerticalStrut, Zoomable }
import org.nlogo.theme.InterfaceColors
import org.nlogo.util.Implicits.RichString
import org.nlogo.window.{ AutoIndentHandler, EditorAreaErrorLabel, EditorColorizer }

class EditorPanel(compiler: CompilerServices, colorizer: EditorColorizer)
  extends JPanel with Transparent with Zoomable {

  val comboBox = new PreviewCommandsComboBox
  val compileButton = new Button("", () => {
    if (dirty) {
      dirty = false
      updateCompileIcon()
      comboBox.updateCommands(PreviewCommands(editor.getText()))
    }
  }) with PreferredSize {
    override def getPreferredSize: Dimension = {
      val size: Int = comboBox.getPreferredSize.height

      new Dimension(size, size)
    }
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
  val editor = new EditorArea(configuration) with AutoIndentHandler {
    setBackground(InterfaceColors.codeBackground())
    setCaretColor(InterfaceColors.textAreaText())

    override def getPreferredSize: Dimension =
      new Dimension(Utils.zoom(350), Utils.zoom(100))
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

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))

  add(new BoxRow(Seq(
    comboBox,
    new HorizontalStrut(6),
    compileButton
  )) {
    override def getMaximumSize: Dimension =
      new Dimension(super.getMaximumSize.width, getPreferredSize.height)
  })

  add(new VerticalStrut(6))
  add(errorLabel)

  add(new ScrollPane(editor) {
    setBackground(InterfaceColors.codeBackground())
  })

  def update(previewCommands: PreviewCommands): Unit = {
    editor.setText(previewCommands.source)
    editor.setEnabled(previewCommands.isInstanceOf[Compilable])
    dirty = false
    updateCompileIcon()
  }

  private def updateCompileIcon(): Unit = {
    val size: Int = Utils.zoom(15)

    compileButton.setIcon(Utils.iconScaledWithColor("/images/check.png", size, size,
                                                    if (dirty) {
                                                      InterfaceColors.checkFilled()
                                                    } else {
                                                      InterfaceColors.toolbarImage()
                                                    }))
  }

  override def zoom(oldZoom: Float): Unit = {
    updateCompileIcon()
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
