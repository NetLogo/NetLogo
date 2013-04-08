// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import scala.Array.fallbackCanBuildFrom
import scala.Option.option2Iterable

import org.nlogo.api.ModelRun
import org.nlogo.awt.UserCancelException
import org.nlogo.util.Exceptions.ignoring

import javax.swing.{ AbstractAction, ImageIcon, JButton, JCheckBox, JFileChooser, JOptionPane }
import javax.swing.filechooser.FileNameExtensionFilter

class ActionButton(name: String, icon: String, fn: () => Unit)
  extends JButton(new ReviewAction(name, icon, fn))

class ReviewToolBar(reviewTab: ReviewTab)
  extends org.nlogo.swing.ToolBar {
  import ReviewToolBar._

  val saveButton = new ActionButton("Save", "save", () => saveRun(reviewTab))
  val loadButton = new ActionButton("Load", "open", () => loadRun(reviewTab))
  val renameButton = new ActionButton("Rename", "edit", () => rename(reviewTab))
  val closeCurrentButton = new ActionButton("Close", "close", () => closeCurrentRun(reviewTab))
  val closeAllButton = new ActionButton("Close all", "close-all", () => closeAll(reviewTab))
  val enabledCheckBox = new EnabledCheckBox(reviewTab.tabState)
  override def addControls() {
    add(saveButton)
    add(loadButton)
    add(renameButton)
    add(closeCurrentButton)
    add(closeAllButton)
    add(new org.nlogo.swing.ToolBar.Separator)
    add(enabledCheckBox)
  }
}

object ReviewToolBar {

  def saveRun(reviewTab: ReviewTab) {
    for (run <- reviewTab.tabState.currentRun) {
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
        reviewTab.tabState.undirty(run)
        reviewTab.refreshInterface()
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
    val results: Seq[Either[String, ModelRun]] =
      chooseFiles.map { path =>
        // Load a run from `path` and returns either the loaded run
        // in case of success or the path in case of failure
        try {
          reviewTab.loadRun(new java.io.FileInputStream(path))
          val run = reviewTab.tabState.runs.last
          Right(run)
        } catch {
          case ex: Exception => Left(path)
        }
      }
    val loadedRuns = results.flatMap(_.right.toOption)
    // select the last loaded run if we have one:
    loadedRuns.lastOption.foreach { run =>
      reviewTab.runList.setSelectedValue(run, true)
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
  }

  def rename(reviewTab: ReviewTab) {
    for {
      run <- reviewTab.tabState.currentRun
      icon = new ImageIcon(classOf[ReviewTab].getResource("/images/edit.gif"))
      answer <- Option(JOptionPane.showInputDialog(reviewTab,
        "Please enter new name:",
        "Rename run",
        JOptionPane.PLAIN_MESSAGE, icon, null, run.name)
        .asInstanceOf[String])
      if answer.nonEmpty
    } {
      run.name = answer
      reviewTab.refreshInterface()
    }
  }

  def closeCurrentRun(reviewTab: ReviewTab) {
    for (run <- reviewTab.tabState.currentRun) {
      if (!run.dirty ||
        reviewTab.userConfirms("Close current run",
          "The current run has unsaved data. Are you sure you want to close the current run?")) {
        reviewTab.tabState.closeCurrentRun()
        // select the new current run if there is one:
        reviewTab.tabState.currentRun.foreach(reviewTab.runList.setSelectedValue(_, true))
        reviewTab.refreshInterface()
      }
    }
  }

  def closeAll(reviewTab: ReviewTab) {
    if (!reviewTab.tabState.dirty ||
      reviewTab.userConfirms("Close all runs",
        "Some runs have unsaved data. Are you sure you want to close all runs?")) {
      reviewTab.tabState.reset()
      reviewTab.refreshInterface()
    }
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
