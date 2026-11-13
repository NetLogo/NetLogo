// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.nio.file.Path

import org.nlogo.api.LabProtocol
import org.nlogo.core.LocaleContext
import org.nlogo.swing.{ BoxAlign, BoxRow }
import org.nlogo.window.{ BooleanEditor, EditPanel, FilePathEditor, IntegerEditor, LabeledEditor, OptionsEditor,
                          PositiveIntegerEditor, PropertyAccessor, PropertyEditor }

class RunOptionsEditPanel(target: RunOptionsDialog#EditableRunOptions, currentDirectory: Option[Path],
                          spreadsheetFile: String, tableFile: String, statsFile: String, listsFile: String,
                          defaultProcessors: String, totalProcessors: String)
  extends EditPanel(target) {

  private val spreadsheet =
    new FilePathEditor(
      new PropertyAccessor(
        target,
        loc"Spreadsheet output",
        () => target.spreadsheet,
        _.foreach(target.setSpreadsheet),
        () => apply()),
      this, currentDirectory, Option(spreadsheetFile))

  private val table =
    new FilePathEditor(
      new PropertyAccessor(
        target,
        loc"Table output",
        () => target.table,
        _.foreach(target.setTable),
        () => apply()),
      this, currentDirectory, Option(tableFile))

  private val stats =
    new FilePathEditor(
      new PropertyAccessor(
        target,
        loc"Stats output",
        () => target.stats,
        _.foreach(target.setStats),
        () => apply()),
      this, currentDirectory, Option(statsFile))

  private val lists =
    new FilePathEditor(
      new PropertyAccessor(
        target,
        loc"Lists output",
        () => target.lists,
        _.foreach(target.setLists),
        () => apply()),
      this, currentDirectory, Option(listsFile))

  private val updateView =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"Update view",
        () => target.updateView,
        _.foreach(target.setUpdateView),
        () => apply()))

  private val updatePlotsAndMonitors =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"Update plots and monitors",
        () => target.updatePlotsAndMonitors,
        _.foreach(target.setUpdatePlotsAndMonitors),
        () => apply()))

  private val updateLabeled =
    new LabeledEditor(updatePlotsAndMonitors, loc"""|<html>When unchecked performance will be better. However, no plot
                                                    |data can be exported,<br> and you will not be able to toggle plots
                                                    |in the "Running Experiment" window.</html>""")

  private val threadCount =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        loc"Simultaneous runs in parallel",
        () => target.threadCount,
        _.foreach(target.setThreadCount),
        () => apply()))

  private val threadCountLabeled =
    new LabeledEditor(threadCount, loc"""|<html>If more than one, some runs happen invisibly in the background.
                                         |Default: $defaultProcessors runs. Recommended maximum: $totalProcessors
                                         |runs.</html>""")

  private val memoryLimit =
    new PositiveIntegerEditor(
      new PropertyAccessor(
        target,
        loc"Experiment memory limit (MB)",
        () => target.memoryLimit,
        _.foreach(target.setMemoryLimit),
        () => apply()))

  private val memoryLimitLabeled = new LabeledEditor(memoryLimit, loc"0 = no limit")

  private val mirrorHeadlessOutput =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"Display parallel run output in Command Center",
        () => target.mirrorHeadlessOutput,
        _.foreach(target.setMirrorHeadlessOutput),
        () => apply()))

  private val errorBehavior =
    new OptionsEditor[LabProtocol.ErrorBehavior](
      new PropertyAccessor(
        target,
        loc"Error behavior",
        () => target.errorBehavior,
        _.foreach(target.setErrorBehavior),
        () => apply()))

  add(spreadsheet)
  add(table)
  add(stats)
  add(lists)
  add(new BoxRow(updateView, BoxAlign.Start))
  add(updateLabeled)
  add(threadCountLabeled)
  add(new BoxRow(errorBehavior, BoxAlign.Start))
  add(memoryLimitLabeled)
  add(new BoxRow(mirrorHeadlessOutput, BoxAlign.Start))

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(spreadsheet, table, stats, lists, updateView, updatePlotsAndMonitors, threadCount, errorBehavior, memoryLimit,
        mirrorHeadlessOutput)

  // since this edit panel's changes are not saved in the model file,
  // always return false so the model doesn't get marked as dirty (Isaac B 6/27/25)
  override def changed: Boolean = false

  override def syncExtraComponents(): Unit = {
    updateLabeled.syncTheme()
    threadCountLabeled.syncTheme()
    memoryLimitLabeled.syncTheme()
  }

  override def requestFocus(): Unit = {
    spreadsheet.requestFocus()
  }
}
