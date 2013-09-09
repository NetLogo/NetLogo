// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import scala.Array.fallbackCanBuildFrom
import scala.Option.option2Iterable

import org.nlogo.awt.UserCancelException
import org.nlogo.mirror.ModelRun
import org.nlogo.util.Exceptions.ignoring

import javax.swing.AbstractAction
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

class ActionButton(name: String, icon: String, fn: () => Unit)
  extends JButton(new ReviewAction(name, icon, fn))

class RunRecorderSub(runRecorderPub: RunRecorder#Pub, refreshButtons: () => Unit) extends RunRecorder#Sub {
  runRecorderPub.subscribe(this)
  override def notify(pub: RunRecorder#Pub, event: RunRecorderEvent) {
    event match {
      case FrameAddedEvent(_, _) => refreshButtons()
      case _ =>
    }
  }
}

class ReviewTabStateSub(reviewTabStatePub: ReviewTabState#Pub, refreshButtons: () => Unit)
  extends ReviewTabState#Sub {
  override def notify(reviewTabStatePub: ReviewTabState#Pub, event: CurrentRunChangeEvent) {
    reviewTabStatePub.subscribe(this)
    event match {
      case AfterCurrentRunChangeEvent(_, newRun) => refreshButtons()
      case _ =>
    }
  }
}

class ReviewToolBar(reviewTab: ReviewTab, runRecorderPub: RunRecorder#Pub)
  extends org.nlogo.swing.ToolBar {

  val saveButton = new ActionButton("Save", "save", () => saveRun(reviewTab))
  val loadButton = new ActionButton("Load", "open", () => loadRun(reviewTab))
  val renameButton = new ActionButton("Rename", "edit", () => rename(reviewTab))
  val closeCurrentButton = new ActionButton("Close", "close", () => closeCurrentRun())
  val closeAllButton = new ActionButton("Close all", "close-all", () => closeAll())
  val enabledCheckBox = new EnabledCheckBox(reviewTab.state)

  refreshButtons()

  val runRecorderSub = new RunRecorderSub(runRecorderPub, refreshButtons)
  val reviewTabStateSub = new ReviewTabStateSub(reviewTab.state, refreshButtons)

  override def addControls() {
    add(saveButton)
    add(loadButton)
    add(renameButton)
    add(closeCurrentButton)
    add(closeAllButton)
    add(new org.nlogo.swing.ToolBar.Separator)
    add(enabledCheckBox)
  }

  def refreshButtons(): Unit = {
    val currentRunDefined = reviewTab.state.currentRun.isDefined
    val currentRunDirty = reviewTab.state.currentRun.map(_.dirty).getOrElse(false)
    Seq(renameButton, closeCurrentButton, closeAllButton)
      .foreach(_.setEnabled(currentRunDefined))
    saveButton.setEnabled(currentRunDefined)
  }

  def saveRun(reviewTab: ReviewTab) {
    for (run <- reviewTab.state.currentRun) {
      ignoring(classOf[UserCancelException]) {
        val path = org.nlogo.swing.FileDialog.show(
          reviewTab, "Save Run", java.awt.FileDialog.SAVE,
          run.name + ".nlrun")
        if (new java.io.File(path).exists &&
          !reviewTab.userConfirms("Save Model Run", "The file " + path +
            " already exists. Do you want to overwrite it?"))
          throw new UserCancelException
        run.name = ReviewTab.removeExtension(path)
        run.save(new java.io.FileOutputStream(path))
        run.dirty = false
        saveButton.setEnabled(false)
        reviewTab.runList.repaint()
      }
    }
  }

  private def chooseFiles: Seq[String] = {
    val fc = new JFileChooser()
    fc.setDialogTitle("Open NetLogo Model Run(s)")
    fc.setFileFilter(new FileNameExtensionFilter(
      "NetLogo Model Runs (*.nlrun)", "nlrun"))
    fc.setMultiSelectionEnabled(true)
    if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
      fc.getSelectedFiles.map(_.getPath())
    else
      Seq()
  }

  def loadRun(reviewTab: ReviewTab) {
    // TODO: this whole thing is a bit convoluted.
    // There might be a way to do it more cleanly
    // and (more importantly) without loosing the
    // full exception information when something
    // goes wrong. NP 2013-04-25.
    try {
      val results: Seq[Either[String, ModelRun]] =
        chooseFiles.map { path =>
          // Load a run from `path` and returns either the loaded run
          // in case of success or the path in case of failure
          try {
            reviewTab.loadRun(new java.io.FileInputStream(path))
            val run = reviewTab.state.runs.last
            Right(run)
          } catch {
            case e: UserCancelException => throw e // stop now if user cancels
            case e: Exception => Left(path) // accumulate other exceptions
          }
        }
      val loadedRuns = results.flatMap(_.right.toOption)
      // select the last loaded run if we have one:
      loadedRuns.lastOption.foreach { run =>
        reviewTab.runList.setSelectedValue(run, true)
        saveButton.setEnabled(false)
      }
      val errors = results.flatMap(_.left.toOption)
      if (errors.nonEmpty) {
        val (notStr, fileStr) =
          if (errors.size > 1) ("they are not", "files")
          else ("it is not a", "file")
        val msg = "Something went wrong while trying to load the following " +
          fileStr + ":\n\n" + errors.mkString("\n") + "\n\n" +
          "Maybe " + notStr + " proper NetLogo Model Run " + fileStr + "?"
        JOptionPane.showMessageDialog(reviewTab, msg, "NetLogo", JOptionPane.ERROR_MESSAGE);
      }
    } catch {
      case _: UserCancelException => // do nothing
      case e: Exception => throw e // rethrow anything else
    }
  }

  def rename(reviewTab: ReviewTab) {
    for {
      run <- reviewTab.state.currentRun
      icon = new ImageIcon(classOf[ReviewTab].getResource("/images/edit.gif"))
      answer <- Option(JOptionPane.showInputDialog(reviewTab,
        "Please enter new name:",
        "Rename run",
        JOptionPane.PLAIN_MESSAGE, icon, null, run.name)
        .asInstanceOf[String])
      if answer.nonEmpty
    } {
      run.name = answer
      run.dirty = true
      saveButton.setEnabled(true)
    }
  }

  def closeCurrentRun(): Int = {
    import JOptionPane._
    def confirm(run: ModelRun): Int = {
      val message = "The following run:\n\n" + run.name + "\n\n" +
        "...has unsaved data. Do you want to save it before closing?"
      JOptionPane.showConfirmDialog(reviewTab, message,
        "Close run", YES_NO_CANCEL_OPTION)
    }
    def close() {
      reviewTab.state.closeCurrentRun()
      // select the new current run if there is one:
      for (run <- reviewTab.state.currentRun)
        reviewTab.runList.setSelectedValue(run, true)
    }
    reviewTab.state.currentRun.map { run =>
      val answer =
        if (run.dirty) confirm(run)
        else NO_OPTION // if run not dirty, don't save
      answer match {
        case YES_OPTION =>
          saveRun(reviewTab)
          close()
        case NO_OPTION =>
          close()
        case CANCEL_OPTION => // do nothing
      }
      refreshButtons()
      answer
    }.getOrElse(CANCEL_OPTION) // no current run returns CANCEL
  }

  def closeAll(): Unit = {
    Iterator.continually(closeCurrentRun())
      .find(_ == JOptionPane.CANCEL_OPTION) // user cancels or no more runs
  }
}

class EnabledCheckBox(tabState: ReviewTabState) extends JCheckBox {
  setSelected(tabState.recordingEnabled)
  setAction(new AbstractAction("Recording") {
    def actionPerformed(e: java.awt.event.ActionEvent) {
      tabState.recordingEnabled = !tabState.recordingEnabled
    }
  })
}
