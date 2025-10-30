// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Frame }
import java.awt.event.{ ActionEvent, ItemEvent, ItemListener }
import java.beans.{ PropertyChangeEvent, PropertyChangeListener }
import javax.swing.{ AbstractAction, JDialog, JPanel }

import org.nlogo.api.PreviewCommands
import org.nlogo.awt.Positioning
import org.nlogo.core.{ AgentKind, CompilerException, I18N, Model }
import org.nlogo.swing.{ AutomateWindow, Button, Transparent, Utils }
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.{ EditorColorizer, GraphicsPreviewInterface }
import org.nlogo.workspace.{ Evaluator, WorkspaceFactory }
import org.nlogo.workspace.ModelsLibrary.getImagePath
import org.nlogo.workspace.PreviewCommandsRunner.initWorkspace

class PreviewCommandsDialog(
  owner: Frame,
  title: String,
  model: Model,
  modelPath: String,
  workspaceFactory: WorkspaceFactory,
  graphicsPreview: GraphicsPreviewInterface)
  extends JDialog(owner, title, true) with AutomateWindow {

  private val workspace = initWorkspace(workspaceFactory, _.openModel(model))

  private var _previewCommands =
    model.optionalSectionValue[PreviewCommands]("org.nlogo.modelsection.previewcommands")
      .getOrElse(PreviewCommands.Default)
  def previewCommands = _previewCommands

  val guiState = new GUIState(model, modelPath, workspaceFactory)
  val editorPanel = new EditorPanel(workspace, new EditorColorizer(workspace))
  val comboBox = editorPanel.comboBox
  val editor = editorPanel.editor
  val previewPanel = new PreviewPanel(graphicsPreview)

  val cancelAction = new AbstractAction(I18N.gui.get("common.buttons.cancel")) {
    def actionPerformed(evt: ActionEvent): Unit = {
      workspace.dispose()
      setVisible(false)
    }
  }

  Utils.addEscKeyAction(this, cancelAction)

  val okButton = new Button(I18N.gui.get("common.buttons.ok"), () => {
    for (previewCommands <- guiState.previewCommands) {
      _previewCommands = previewCommands
      workspace.dispose()
      setVisible(false)
    }
  })

  getRootPane.setDefaultButton(okButton)

  add(new JPanel(new BorderLayout) {
    setBackground(InterfaceColors.dialogBackground())
    add(editorPanel, BorderLayout.CENTER)
    add(previewPanel, BorderLayout.LINE_END)
    add(new JPanel with Transparent {
      add(okButton)
      add(new Button(cancelAction))
    }, BorderLayout.PAGE_END)
  })

  comboBox.addItemListener(new ItemListener {
    def itemStateChanged(evt: ItemEvent): Unit =
      if (evt.getStateChange == ItemEvent.SELECTED) {
        evt.getItem match {
          case Some(PreviewCommandsWrapper(pc: PreviewCommands)) =>
            editorPanel.update(pc)
            guiState.previewCommands = pc

          case _ =>
        }
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
      val setError = editorPanel.errorLabel.setError
      evt.getNewValue match {
        case Some(e: CompilerException) =>
          setError(Option(e), Evaluator.sourceOffset(AgentKind.Observer, true))
          okButton.setEnabled(false)
        case _ =>
          setError(None, 0)
          okButton.setEnabled(true)
      }
    }
  })

  comboBox.updateCommands(previewCommands)

  pack()
  Positioning.center(this, owner)
}
