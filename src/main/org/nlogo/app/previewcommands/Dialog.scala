package org.nlogo.app.previewcommands

import java.awt.BorderLayout
import java.awt.Frame
import java.awt.event.ActionEvent
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import org.nlogo.api.CompilerException
import org.nlogo.api.I18N
import org.nlogo.api.PreviewCommands
import org.nlogo.awt.Positioning.center
import org.nlogo.headless.HeadlessWorkspace
import org.nlogo.swing.Utils.addEscKeyAction
import org.nlogo.window.EditorColorizer
import org.nlogo.window.GraphicsPreviewInterface
import org.nlogo.workspace.Evaluator
import org.nlogo.workspace.ModelsLibrary.getImagePath
import javax.swing.AbstractAction
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel
import org.nlogo.nvm.WorkspaceFactory

class Dialog(
  owner: Frame,
  title: String,
  modelContents: String,
  modelPath: String,
  workspaceFactory: WorkspaceFactory,
  graphicsPreview: GraphicsPreviewInterface)
  extends JDialog(owner, title, true) {
  org.nlogo.awt.Fonts.adjustDefaultFont(this)

  val ws = HeadlessWorkspace.newInstance
  ws.openString(modelContents)
  private var _previewCommands = ws.previewCommands
  def previewCommands = _previewCommands

  val guiState = new GUIState(modelContents, ws, workspaceFactory)
  val editorPanel = new EditorPanel(new EditorColorizer(ws))
  val comboBox = editorPanel.comboBox
  val editor = editorPanel.editor
  val previewPanel = new PreviewPanel(graphicsPreview)

  comboBox.addItemListener(new ItemListener() {
    def itemStateChanged(evt: ItemEvent): Unit =
      if (evt.getStateChange == ItemEvent.SELECTED) {
        val previewCommands = evt.getItem.asInstanceOf[PreviewCommands]
        editorPanel.update(previewCommands)
        guiState.previewCommands = previewCommands
      }
  })

  editorPanel.textListener.addPropertyChangeListener(new PropertyChangeListener {
    override def propertyChange(evt: PropertyChangeEvent): Unit = {
      editorPanel.compileButton.setEnabled(true)
      previewPanel.button.setEnabled(true)
    }
  })

  guiState.addPropertyChangeListener("previewCommands", new PropertyChangeListener {
    override def propertyChange(evt: PropertyChangeEvent): Unit =
      previewPanel.button.setAction(
        evt.getNewValue match {
          case _: PreviewCommands.Compilable =>
            previewPanel.executeCommandsAction(guiState.runnablePreviewCommands)
          case _ =>
            previewPanel.loadManualPreviewAction(Option(modelPath).map(getImagePath))
        }
      )
  })

  guiState.addPropertyChangeListener("compilerException", new PropertyChangeListener {
    override def propertyChange(evt: PropertyChangeEvent): Unit = {
      val setError = editorPanel.errorLabel.setError _
      evt.getNewValue match {
        case Some(e: CompilerException) => setError(e, Evaluator.sourceOffset(ws.world.observers.kind, true))
        case _ => setError(null, 0)
      }
    }
  })

  comboBox.updateCommands(previewCommands)

  val cancelAction = new AbstractAction(I18N.guiJ.get("common.buttons.cancel")) {
    def actionPerformed(evt: ActionEvent): Unit = setVisible(false)
  }
  addEscKeyAction(this, cancelAction)

  val okAction = new AbstractAction(I18N.guiJ.get("common.buttons.ok")) {
    def actionPerformed(evt: ActionEvent): Unit = {
      _previewCommands = guiState.previewCommands
      setVisible(false)
    }
  }

  add(new JPanel() {
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))
    setLayout(new BorderLayout)
    add(editorPanel, BorderLayout.CENTER)
    add(previewPanel, BorderLayout.LINE_END)
    add(new JPanel() {
      add(new JButton(okAction))
      add(new JButton(cancelAction))
    }, BorderLayout.PAGE_END)
  })
  pack()
  center(this, owner)
}
