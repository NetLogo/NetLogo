// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import java.awt.BorderLayout

import org.nlogo.api
import org.nlogo.mirror.ModelRunIO
import org.nlogo.window
import org.nlogo.window.ModelLoader

import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.TableModelEvent
import javax.swing.event.TableModelListener

class ReviewTab(
  val ws: window.GUIWorkspace,
  val getCurrentModelString: () => String,
  offerSave: () => Unit,
  selectReviewTab: () => Unit)
  extends JPanel
  with window.ReviewTabInterface {

  val state = new ReviewTabState(ws)
  def recordingEnabled = state.recordingEnabled
  def recordingEnabled_=(enabled: Boolean): Unit = state.recordingEnabled = enabled

  val runList = new RunList(this)

  val runRecorder = new RunRecorder(ws, state, getCurrentModelString)

  override def loadedRuns: Seq[api.ModelRun] = state.runs
  override def loadRun(inputStream: java.io.InputStream): Unit = {
    val run = ModelRunIO.load(inputStream)
    if (!isLoaded(run.modelString)) loadModel(run.modelString)
    state.addRun(run)
  }
  override def currentRun: Option[api.ModelRun] = state.currentRun

  def userConfirms(title: String, message: String) =
    JOptionPane.showConfirmDialog(ReviewTab.this, message,
      title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION

  val notesTabbedPane = new NotesTabbedPane(state)
  val scrubberPanel = new ScrubberPanel(
    notesTabbedPane.indexedNotesTable,
    () => state.currentFrameIndex,
    state.afterRunChangePub,
    runRecorder.frameAddedPub)
  val reviewToolBar = new ReviewToolBar(this, runRecorder.frameAddedPub)
  val interfacePanel = new InterfacePanel(this)

  scrubberPanel.scrubber.addChangeListener(new ChangeListener {
    def stateChanged(evt: ChangeEvent) {
      val newFrame = scrubberPanel.scrubber.getValue
      state.currentRun.foreach(_.currentFrameIndex = Some(newFrame))
      notesTabbedPane.indexedNotesTable.scrollTo(newFrame)
      interfacePanel.widgetPanels.foreach(_.invalidate())
      interfacePanel.repaint()
    }
  })

  // TODO: this should probably be in state
  notesTabbedPane.generalNotes.getDocument.addDocumentListener(new DocumentListener {
    private def updateGeneralNotesInRun() {
      for (run <- state.currentRun) {
        run.generalNotes = notesTabbedPane.generalNotes.getText
        reviewToolBar.saveButton.setEnabled(run.dirty)
      }
    }
    def insertUpdate(e: DocumentEvent) { updateGeneralNotesInRun() }
    def removeUpdate(e: DocumentEvent) { updateGeneralNotesInRun() }
    def changedUpdate(e: DocumentEvent) { updateGeneralNotesInRun() }
  })

  // TODO: this should probably be in state
  notesTabbedPane.indexedNotesTable.getModel.addTableModelListener(new TableModelListener {
    override def tableChanged(event: TableModelEvent) {
      for (run <- state.currentRun) {
        run.indexedNotes = notesTabbedPane.indexedNotesTable.model.notes
        reviewToolBar.saveButton.setEnabled(run.dirty)
      }
    }
  })

  def isLoaded(modelString: String): Boolean =
    getCurrentModelString() == modelString

  def loadModel(modelString: String) {
    offerSave()
    ModelLoader.load(ReviewTab.this, null, api.ModelType.Library, modelString)
    selectReviewTab()
  }

  object RunListPanel extends JPanel {
    setLayout(new BorderLayout)
    add(new JScrollPane(runList), BorderLayout.CENTER)
  }

  object InterfaceScrollPane extends JScrollPane(interfacePanel)

  object RunPanel extends JPanel {
    setLayout(new BorderLayout)
    add(InterfaceScrollPane, BorderLayout.CENTER)
    add(scrubberPanel, BorderLayout.SOUTH)
  }

  object PrimarySplitPane extends JSplitPane(
    JSplitPane.VERTICAL_SPLIT,
    SecondarySplitPane,
    notesTabbedPane) {
    setResizeWeight(0.8)
    setDividerLocation(400)
  }

  object SecondarySplitPane extends JSplitPane(
    JSplitPane.HORIZONTAL_SPLIT,
    RunListPanel,
    RunPanel) {
    setResizeWeight(0.0)
    setDividerLocation(190)
  }

  locally {
    setLayout(new BorderLayout)
    add(reviewToolBar, BorderLayout.NORTH)
    add(PrimarySplitPane, BorderLayout.CENTER)
  }
}
