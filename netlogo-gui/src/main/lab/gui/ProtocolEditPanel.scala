// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.awt.{ GridBagConstraints, Insets }
import javax.swing.{ JLabel, JPanel }

import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.{ BooleanEditor, CodeEditor, EditPanel, IntegerEditor, PropertyAccessor, PropertyEditor,
                          ReporterLineEditor, StringEditor }

class ProtocolEditPanel(target: ProtocolEditable, compiler: CompilerServices, colorizer: Colorizer)
  extends EditPanel(target) {

  private val hintLabel = new JLabel(s"<html>${I18N.gui.get("tools.behaviorSpace.hint")}</html>")
  private val hintPanel = new JPanel {
    add(hintLabel)
  }

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
    val c = new GridBagConstraints

    c.gridx = 0
    c.gridwidth = 2
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(hintPanel, c)

    c.insets = new Insets(0, 6, 6, 6)

    add(name, c)
    add(valueSets, c)
    add(repetitions, c)
    add(sequentialRunOrder, c)
    add(metrics, c)
    add(runMetricsEveryStep, c)
    add(runMetricsCondition, c)
    add(preExperimentCommands, c)

    c.gridwidth = 1

    add(setupCommands, c)

    c.gridx = 1

    add(goCommands, c)

    c.gridx = 0
    c.anchor = GridBagConstraints.NORTH
    c.weighty = 1

    add(exitCondition, c)

    c.gridx = 1

    add(postRunCommands, c)

    c.gridx = 0

    add(postExperimentCommands, c)

    c.gridwidth = 2

    add(timeLimit, c)
  }

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(name, valueSets, repetitions, sequentialRunOrder, metrics, runMetricsEveryStep, runMetricsCondition,
        preExperimentCommands, setupCommands, goCommands, exitCondition, postRunCommands, postExperimentCommands,
        timeLimit)

  override def syncExtraComponents(): Unit = {
    hintPanel.setBackground(InterfaceColors.bspaceHintBackground())
    hintLabel.setForeground(InterfaceColors.dialogText())
  }

  override def requestFocus(): Unit = {
    name.requestFocus()
  }
}
