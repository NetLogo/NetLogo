// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.awt.{ GridBagConstraints, Insets }
import javax.swing.JLabel

import org.nlogo.core.I18N
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.{ BooleanEditor, EditPanel, FilePathEditor, IntegerEditor, PropertyAccessor, PropertyEditor }

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

  private val updateLabel = new JLabel(s"<html>${
    I18N.gui.get("tools.behaviorSpace.runoptions.updateplotsandmonitors.info")}</html>") {

    setFont(getFont.deriveFont(9.0f))
  }

  private val threadCount =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.runoptions.simultaneousruns"),
        () => target.threadCount,
        target.setThreadCount(_),
        () => apply()))

  private val threadCountLabel = new JLabel(s"<html>${I18N.gui.getN("tools.behaviorSpace.runoptions.simultaneousruns.info", defaultProcessors,
                                                      totalProcessors)}</html>") {
    setFont(getFont.deriveFont(9.0f))
  }

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

    c.insets = new Insets(0, 6, 3, 6)

    add(updatePlotsAndMonitors, c)

    c.insets = new Insets(0, 6, 6, 6)

    add(updateLabel, c)

    c.insets = new Insets(0, 6, 3, 6)

    add(threadCount, c)

    c.insets = new Insets(0, 6, 6, 6)

    add(threadCountLabel, c)

    spreadsheet.requestFocus()
  }

  override def propertyEditors: Seq[PropertyEditor[_]] =
    Seq(spreadsheet, table, stats, lists, updateView, updatePlotsAndMonitors, threadCount)

  override def syncExtraComponents(): Unit = {
    updateLabel.setForeground(InterfaceColors.dialogText)
    threadCountLabel.setForeground(InterfaceColors.dialogText)
  }
}
