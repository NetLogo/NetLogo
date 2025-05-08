// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.awt.{ GridBagConstraints, Insets }

import org.nlogo.core.I18N
import org.nlogo.window.{ BooleanEditor, EditPanel, FilePathEditor, IntegerEditor, LabeledEditor, PropertyAccessor,
                          PropertyEditor }

class RunOptionsEditPanel(target: RunOptionsDialog#EditableRunOptions, spreadsheetFile: String, tableFile: String,
                          statsFile: String, listsFile: String, defaultProcessors: String, totalProcessors: String)
  extends EditPanel(target) {

  private val spreadsheet =
    new FilePathEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.runoptions.spreadsheet"),
        () => target.spreadsheet,
        target.setSpreadsheet(_),
        () => apply()),
      this, Option(spreadsheetFile))

  private val table =
    new FilePathEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.runoptions.table"),
        () => target.table,
        target.setTable(_),
        () => apply()),
      this, Option(tableFile))

  private val stats =
    new FilePathEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.runoptions.stats"),
        () => target.stats,
        target.setStats(_),
        () => apply()),
      this, Option(statsFile))

  private val lists =
    new FilePathEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.runoptions.lists"),
        () => target.lists,
        target.setLists(_),
        () => apply()),
      this, Option(listsFile))

  private val updateView =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.runoptions.updateview"),
        () => target.updateView,
        target.setUpdateView(_),
        () => apply()))

  private val updatePlotsAndMonitors =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.runoptions.updateplotsandmonitors"),
        () => target.updatePlotsAndMonitors,
        target.setUpdatePlotsAndMonitors(_),
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
        target.setThreadCount(_),
        () => apply()))

  private val threadCountLabeled =
    new LabeledEditor(threadCount, s"<html>${I18N.gui.getN("tools.behaviorSpace.runoptions.simultaneousruns.info",
                                                           defaultProcessors, totalProcessors)}</html>")

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(spreadsheet, c)

    c.insets = new Insets(0, 6, 6, 6)

    add(table, c)
    add(stats, c)
    add(lists, c)
    add(updateView, c)
    add(updateLabeled, c)
    add(threadCountLabeled, c)

    spreadsheet.requestFocus()
  }

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(spreadsheet, table, stats, lists, updateView, updatePlotsAndMonitors, threadCount)

  override def syncExtraComponents(): Unit = {
    updateLabeled.syncTheme()
    threadCountLabeled.syncTheme()
  }
}
