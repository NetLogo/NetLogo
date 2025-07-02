// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import org.nlogo.analytics.Analytics
import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.swing.DynamicRowLayout
import org.nlogo.window.{ BooleanEditor, CodeEditor, EditPanel, IntegerEditor, PropertyAccessor, PropertyEditor,
                          ReporterLineEditor, StringEditor }

class ProtocolEditPanel(target: ProtocolEditable, compiler: CompilerServices, colorizer: Colorizer)
  extends EditPanel(target) {

  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.experimentName"),
        () => target.name,
        _.foreach(target.setName),
        () => apply())) {

      setToolTipText(I18N.gui.get("tools.behaviorSpace.experimentName.info"))
    }

  private val valueSets =
    new CodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.vary"),
        () => target.valueSets,
        _.foreach(target.setValueSets),
        () => apply()),
      colorizer) {

      setToolTipText(s"<html>${I18N.gui.get("tools.behaviorSpace.vary.info")}</html>")
    }

  private val repetitions =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.repetitions"),
        () => target.repetitions,
        _.foreach(target.setRepetitions),
        () => apply())) {

      setToolTipText(I18N.gui.get("tools.behaviorSpace.repetitions.info"))
    }

  private val sequentialRunOrder =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.sequentialRunOrder"),
        () => target.sequentialRunOrder,
        _.foreach(target.setSequentialRunOrder),
        () => apply())) {

      setToolTipText(s"<html>${I18N.gui.get("tools.behaviorSpace.sequentialRunOrder.info")}</html>")
    }

  private val metrics =
    new CodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.metrics"),
        () => target.metrics,
        _.foreach(target.setMetrics),
        () => apply()),
      colorizer) {

      setToolTipText(s"<html>${I18N.gui.get("tools.behaviorSpace.metrics.info")}</html>")
    }

  private val runMetricsEveryStep: BooleanEditor =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.runMetricsEveryStep"),
        () => target.runMetricsEveryStep,
        _.foreach(target.setRunMetricsEveryStep),
        () => {
          apply()
          runMetricsCondition.setEnabled(!runMetricsEveryStep.get.getOrElse(true))
        })) {

      setToolTipText(I18N.gui.get("tools.behaviorSpace.runMetricsEveryStep.info"))
    }

  private val runMetricsCondition =
    new ReporterLineEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.runMetricsCondition"),
        () => target.runMetricsCondition,
        _.foreach(target.setRunMetricsCondition),
        () => apply()),
      colorizer, true) {

      setToolTipText(I18N.gui.get("tools.behaviorSpace.runMetricsCondition.info"))
      setEnabled(!target.runMetricsEveryStep)
    }

  private val preExperimentCommands =
    new CodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.preExperimentCommands"),
        () => target.preExperimentCommands,
        _.foreach(target.setPreExperimentCommands),
        () => apply()),
      colorizer, true, true) {

      setToolTipText(I18N.gui.get("tools.behaviorSpace.preExperimentCommands.info"))
    }

  private val setupCommands =
    new CodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.setupCommands"),
        () => target.setupCommands,
        _.foreach(target.setSetupCommands),
        () => apply()),
      colorizer) {

      setToolTipText(I18N.gui.get("tools.behaviorSpace.setupCommands.info"))
    }

  private val goCommands =
    new CodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.goCommands"),
        () => target.goCommands,
        _.foreach(target.setGoCommands),
        () => apply()),
      colorizer) {

      setToolTipText(I18N.gui.get("tools.behaviorSpace.goCommands.info"))
    }

  private val exitCondition =
    new CodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.exitCondition"),
        () => target.exitCondition,
        _.foreach(target.setExitCondition),
        () => apply()),
      colorizer, true, true) {

      setToolTipText(I18N.gui.get("tools.behaviorSpace.exitCondition.info"))
    }

  private val postRunCommands =
    new CodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.postRunCommands"),
        () => target.postRunCommands,
        _.foreach(target.setPostRunCommands),
        () => apply()),
      colorizer, true, true) {

      setToolTipText(I18N.gui.get("tools.behaviorSpace.postRunCommands.info"))
    }

  private val postExperimentCommands =
    new CodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.postExperimentCommands"),
        () => target.postExperimentCommands,
        _.foreach(target.setPostExperimentCommands),
        () => apply()),
      colorizer, true, true) {

      setToolTipText(I18N.gui.get("tools.behaviorSpace.postExperimentCommands.info"))
    }

  private val timeLimit =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.behaviorSpace.timeLimit"),
        () => target.timeLimit,
        _.foreach(target.setTimeLimit),
        () => apply())) {

      setToolTipText(I18N.gui.get("tools.behaviorSpace.timeLimit.info"))
    }

  locally {
    val rowLayout = new DynamicRowLayout(this, 6)

    setLayout(rowLayout)

    rowLayout.addRow(Seq(name))
    rowLayout.addRow(Seq(valueSets), expandY = () => true)
    rowLayout.addRow(Seq(repetitions))
    rowLayout.addRow(Seq(sequentialRunOrder))
    rowLayout.addRow(Seq(metrics), expandY = () => true)
    rowLayout.addRow(Seq(runMetricsEveryStep))
    rowLayout.addRow(Seq(runMetricsCondition))
    rowLayout.addRow(Seq(preExperimentCommands), expandY = () => !preExperimentCommands.collapsed)
    rowLayout.addRow(Seq(setupCommands, goCommands), expandY = () => !setupCommands.collapsed || !goCommands.collapsed)
    rowLayout.addRow(Seq(exitCondition, postRunCommands),
                     expandY = () => !exitCondition.collapsed || !postRunCommands.collapsed)
    rowLayout.addRow(Seq(postExperimentCommands), expandY = () => !postExperimentCommands.collapsed)
    rowLayout.addRow(Seq(timeLimit))
  }

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(name, valueSets, repetitions, sequentialRunOrder, metrics, runMetricsEveryStep, runMetricsCondition,
        preExperimentCommands, setupCommands, goCommands, exitCondition, postRunCommands, postExperimentCommands,
        timeLimit)

  override def isResizable: Boolean =
    true

  override def requestFocus(): Unit = {
    name.requestFocus()
  }

  override def setVisible(visible: Boolean): Unit = {
    if (visible)
      Analytics.bspaceOpen()

    super.setVisible(visible)
  }
}
