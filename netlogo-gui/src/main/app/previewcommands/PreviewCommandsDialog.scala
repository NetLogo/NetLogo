package org.nlogo.app.previewcommands

import java.awt.BorderLayout
import java.awt.Frame
import java.awt.event.ActionEvent
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener

import org.nlogo.agent.Observer
import org.nlogo.core.AgentKind
import org.nlogo.core.CompilerException
import org.nlogo.core.I18N
import org.nlogo.api.PreviewCommands
import org.nlogo.awt.Positioning.center
import org.nlogo.swing.Utils.addEscKeyAction
import org.nlogo.window.EditorColorizer
import org.nlogo.window.GraphicsPreviewInterface
import org.nlogo.workspace.Evaluator
import org.nlogo.workspace.ModelsLibrary.getImagePath
import org.nlogo.workspace.PreviewCommandsRunner.initWorkspace
import org.nlogo.workspace.WorkspaceFactory

import javax.swing.AbstractAction
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel

class PreviewCommandsDialog(
  owner: Frame,
  title: String,
  modelContents: String,
  modelPath: String,
  workspaceFactory: WorkspaceFactory,
  graphicsPreview: GraphicsPreviewInterface)
  extends JDialog(owner, title, true) {
  org.nlogo.awt.Fonts.adjustDefaultFont(this)

  private val workspace = initWorkspace(workspaceFactory, _.openString(modelContents))

  private var _previewCommands = workspace.previewCommands
  def previewCommands = _previewCommands

  val guiState = new GUIState(modelContents, modelPath, workspaceFactory)
  val editorPanel = new EditorPanel(new EditorColorizer(workspace))
  val comboBox = editorPanel.comboBox
  val editor = editorPanel.editor
  val previewPanel = new PreviewPanel(graphicsPreview)

  val cancelAction = new AbstractAction(I18N.guiJ.get("common.buttons.cancel")) {
    def actionPerformed(evt: ActionEvent): Unit = {
      workspace.dispose()
      setVisible(false)
    }
  }
  addEscKeyAction(this, cancelAction)

  val okButton = new JButton(new AbstractAction(I18N.guiJ.get("common.buttons.ok")) {
    def actionPerformed(evt: ActionEvent): Unit =
      for (previewCommands <- guiState.previewCommands) {
        _previewCommands = previewCommands
        workspace.dispose()
        setVisible(false)
      }
  })
  getRootPane.setDefaultButton(okButton)

  add(new JPanel() {
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))
    setLayout(new BorderLayout)
    add(editorPanel, BorderLayout.CENTER)
    add(previewPanel, BorderLayout.LINE_END)
    add(new JPanel() {
      add(okButton)
      add(new JButton(cancelAction))
    }, BorderLayout.PAGE_END)
  })

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
    override def propertyChange(evt: PropertyChangeEvent): Unit = {
      previewPanel.button.setAction(
        evt.getNewValue match {
          case Some(_: PreviewCommands.Compilable) =>
            previewPanel.executeCommandsAction(guiState.previewCommandsRunnable)
          case _ =>
            previewPanel.loadManualPreviewAction(Option(modelPath).map(getImagePath))
        }
      )
    }
  })

  guiState.addPropertyChangeListener("compilerException", new PropertyChangeListener {
    override def propertyChange(evt: PropertyChangeEvent): Unit = {
      val setError = editorPanel.errorLabel.setError _
      evt.getNewValue match {
        case Some(e: CompilerException) =>
          setError(e, Evaluator.sourceOffset(AgentKind.Observer, true))
          okButton.setEnabled(false)
        case _ =>
          setError(null, 0)
          okButton.setEnabled(true)
      }
    }
  })

  comboBox.updateCommands(previewCommands)

  pack()
  center(this, owner)
}
