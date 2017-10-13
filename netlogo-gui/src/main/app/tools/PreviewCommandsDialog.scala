// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Frame }
import java.awt.event.{ ActionEvent, ItemEvent, ItemListener }
import java.beans.{ PropertyChangeEvent, PropertyChangeListener }
import javax.swing.{ AbstractAction, BorderFactory, JButton, JDialog, JPanel }

import org.nlogo.api.{ PreviewCommands, Version }
import org.nlogo.awt.{ Fonts, Positioning }
import org.nlogo.core.{ AgentKind, CompilerException, I18N, Model }
import org.nlogo.swing.Utils.addEscKeyAction
import org.nlogo.window.{ DefaultEditorColorizer, GraphicsPreviewInterface }
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
  extends JDialog(owner, title, true) {
  Fonts.adjustDefaultFont(this)

  private val workspace = initWorkspace(
    workspaceFactory,
    Version.getCurrent(model.version),
    _.openModel(model))

  private var _previewCommands =
    model.optionalSectionValue[PreviewCommands]("org.nlogo.modelsection.previewcommands")
      .getOrElse(PreviewCommands.Default)
  def previewCommands = _previewCommands

  val guiState = new GUIState(model, modelPath, workspaceFactory)
  val editorPanel = new EditorPanel(DefaultEditorColorizer(workspace))
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

  add(new JPanel {
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))
    setLayout(new BorderLayout)
    add(editorPanel, BorderLayout.CENTER)
    add(previewPanel, BorderLayout.LINE_END)
    add(new JPanel {
      add(okButton)
      add(new JButton(cancelAction))
    }, BorderLayout.PAGE_END)
  })

  comboBox.addItemListener(new ItemListener {
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
  Positioning.center(this, owner)
}
