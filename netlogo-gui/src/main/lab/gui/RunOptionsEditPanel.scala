// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.nio.file.Path

import org.nlogo.api.LabProtocol
import org.nlogo.core.I18N
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
        I18N.gui.get("tools.behaviorSpace.runoptions.spreadsheet"),
        () => target.spreadsheet,
        _.foreach(target.setSpreadsheet),
        () => apply()),
      this, currentDirectory, Option(spreadsheetFile))

  private val table =
    new FilePathEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.runoptions.table"),
        () => target.table,
        _.foreach(target.setTable),
        () => apply()),
      this, currentDirectory, Option(tableFile))

  private val stats =
    new FilePathEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.runoptions.stats"),
        () => target.stats,
        _.foreach(target.setStats),
        () => apply()),
      this, currentDirectory, Option(statsFile))

  private val lists =
    new FilePathEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.runoptions.lists"),
        () => target.lists,
        _.foreach(target.setLists),
        () => apply()),
      this, currentDirectory, Option(listsFile))

  private val updateView =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.runoptions.updateview"),
        () => target.updateView,
        _.foreach(target.setUpdateView),
        () => apply()))

  private val updatePlotsAndMonitors =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.runoptions.updateplotsandmonitors"),
        () => target.updatePlotsAndMonitors,
        _.foreach(target.setUpdatePlotsAndMonitors),
        () => apply()))

  private val updateLabeled =
    new LabeledEditor(updatePlotsAndMonitors,
                      s"<html>${I18N.gui.get("tools.behaviorSpace.runoptions.updateplotsandmonitors.info")}</html>")

  private val threadCount =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.runoptions.simultaneousruns"),
        () => target.threadCount,
        _.foreach(target.setThreadCount),
        () => apply()))

  private val threadCountLabeled =
    new LabeledEditor(threadCount, s"<html>${I18N.gui.getN("tools.behaviorSpace.runoptions.simultaneousruns.info",
                                                           defaultProcessors, totalProcessors)}</html>")

  private val memoryLimit =
    new PositiveIntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.runoptions.memoryLimit"),
        () => target.memoryLimit,
        _.foreach(target.setMemoryLimit),
        () => apply()))

  private val memoryLimitLabeled =
    new LabeledEditor(memoryLimit, s"<html>${I18N.gui.get("tools.behaviorSpace.runoptions.memoryLimit.info")}</html>")

  private val mirrorHeadlessOutput =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.runoptions.mirrorHeadlessOutput"),
        () => target.mirrorHeadlessOutput,
        _.foreach(target.setMirrorHeadlessOutput),
        () => apply()))

  private val errorBehavior =
    new OptionsEditor[LabProtocol.ErrorBehavior](
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.runoptions.errorBehavior"),
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
