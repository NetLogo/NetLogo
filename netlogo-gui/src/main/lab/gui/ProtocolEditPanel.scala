// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.awt.Dimension

import org.nlogo.api.CompilerServices
import org.nlogo.core.LocaleContext
import org.nlogo.editor.Colorizer
import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, MaximumHeight }
import org.nlogo.window.{ BooleanEditor, CodeEditor, EditPanel, IntegerEditor, PropertyAccessor, PropertyEditor,
                          ReporterLineEditor, StringEditor }

class ProtocolEditPanel(target: ProtocolEditable, compiler: CompilerServices, colorizer: Colorizer)
  extends EditPanel(target) {

  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        loc"Experiment name",
        () => target.name,
        _.foreach(target.setName),
        () => apply())) {

      setToolTipText(loc"experiment names must be unique")
    }

  private val valueSets =
    new CodeEditor(
      new PropertyAccessor(
        target,
        loc"Vary variables as follows (note brackets and quotation marks):",
        () => target.valueSets,
        _.foreach(target.setValueSets),
        () => apply()),
      compiler, colorizer) {

      setToolTipText(loc"""|<html>Either list values to use, for example:<br>
                           |["my-slider" 1 2 7 8]<br>
                           |or specify start, increment, and end, for example:<br>
                           |["my-slider" [0 1 10]] (note additional brackets)<br>
                           |to go from 0, 1 at a time, to 10.<br>
                           |In addition, subexperiments may be specified to create<br>
                           |specific parameter combinations. For example:<br>
                           |[["my-slider" 1 2]["my-slider-2" 3]] (note doubly-nested brackets)<br>
                           |You may also vary max-pxcor, min-pxcor, max-pycor, min-pycor, random-seed.</html>""")
    }

  private val repetitions =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        loc"Repetitions",
        () => target.repetitions,
        _.foreach(target.setRepetitions),
        () => apply())) {

      setToolTipText(loc"execute each combination this many times")
    }

  private val sequentialRunOrder =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"Execute combinations in sequential order",
        () => target.sequentialRunOrder,
        _.foreach(target.setSequentialRunOrder),
        () => apply())) {

      setToolTipText(loc"""|<html>For example, having ["var" 1 2 3] with 2 repetitions,
                           |the experiments'' "var" values will be:<br>
                           |sequential order: 1, 1, 2, 2, 3, 3<br>
                           |alternating order: 1, 2, 3, 1, 2, 3</html>""")
    }

  private val metrics =
    new CodeEditor(
      new PropertyAccessor(
        target,
        loc"Measure runs using these reporters as metrics:",
        () => target.metrics,
        _.foreach(target.setMetrics),
        () => apply()),
      compiler, colorizer) {

      setToolTipText(loc"<html>one reporter per line; you may not split a reporter<br>across multiple lines</html>")
    }

  private val runMetricsEveryStep: BooleanEditor =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"Run metrics every step",
        () => target.runMetricsEveryStep,
        _.foreach(target.setRunMetricsEveryStep),
        () => {
          apply()
          runMetricsCondition.setEnabled(!runMetricsEveryStep.get.getOrElse(true))
        })) {

      setToolTipText(loc"""|when checked, metrics will run at every step, when unchecked, metrics will run at the end
                           |of the experiment or when any "Run metrics when" reporter is true""")
    }

  private val runMetricsCondition =
    new ReporterLineEditor(
      new PropertyAccessor(
        target,
        loc"Run metrics when",
        () => target.runMetricsCondition,
        _.foreach(target.setRunMetricsCondition),
        () => apply()),
      compiler, colorizer, true) {

      setToolTipText(loc"""|metrics will run when any of these reporters are true, for example, use the reporter
                           |"ticks mod 3 = 0" to run metrics every 3 ticks""")

      setEnabled(!target.runMetricsEveryStep)
    }

  private val preExperimentCommands =
    new CodeEditor(
      new PropertyAccessor(
        target,
        loc"Pre experiment commands:",
        () => target.preExperimentCommands,
        _.foreach(target.setPreExperimentCommands),
        () => apply()),
      compiler, colorizer, true, true) {

      setToolTipText(loc"executed before the experiment begins")
    }

  private val setupCommands =
    new CodeEditor(
      new PropertyAccessor(
        target,
        loc"Setup commands:",
        () => target.setupCommands,
        _.foreach(target.setSetupCommands),
        () => apply()),
      compiler, colorizer) {

      setToolTipText(loc"executed before each run begins")
    }

  private val goCommands =
    new CodeEditor(
      new PropertyAccessor(
        target,
        loc"Go commands:",
        () => target.goCommands,
        _.foreach(target.setGoCommands),
        () => apply()),
      compiler, colorizer) {

      setToolTipText(loc"executed at every step")
    }

  private val exitCondition =
    new CodeEditor(
      new PropertyAccessor(
        target,
        loc"Stop condition:",
        () => target.exitCondition,
        _.foreach(target.setExitCondition),
        () => apply()),
      compiler, colorizer, true, true) {

      setToolTipText(loc"the run stops if this reporter becomes true")
    }

  private val postRunCommands =
    new CodeEditor(
      new PropertyAccessor(
        target,
        loc"Post run commands:",
        () => target.postRunCommands,
        _.foreach(target.setPostRunCommands),
        () => apply()),
      compiler, colorizer, true, true) {

      setToolTipText(loc"executed at the end of each run")
    }

  private val postExperimentCommands =
    new CodeEditor(
      new PropertyAccessor(
        target,
        loc"Post experiment commands",
        () => target.postExperimentCommands,
        _.foreach(target.setPostExperimentCommands),
        () => apply()),
      compiler, colorizer, true, true) {

      setToolTipText(loc"executed after the experiment is finished")
    }

  private val timeLimit =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        loc"Time limit",
        () => target.timeLimit,
        _.foreach(target.setTimeLimit),
        () => apply())) {

      setToolTipText(loc"stop after this many steps (0 = no limit)")
    }

  add(name)
  add(valueSets)
  add(repetitions)
  add(new BoxRow(sequentialRunOrder, BoxAlign.Start))
  add(metrics)
  add(new BoxRow(runMetricsEveryStep, BoxAlign.Start))
  add(runMetricsCondition)
  add(preExperimentCommands)
  add(new BoxRow(Seq(
    new BoxColumn(setupCommands, BoxAlign.Start),
    new BoxColumn(goCommands, BoxAlign.Start)
  ), 6))
  add(new BoxRow(Seq(
    new BoxColumn(exitCondition, BoxAlign.Start),
    new BoxColumn(postRunCommands, BoxAlign.Start)
  ), 6) with MaximumHeight)
  add(postExperimentCommands)
  add(timeLimit)

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(name, valueSets, repetitions, sequentialRunOrder, metrics, runMetricsEveryStep, runMetricsCondition,
        preExperimentCommands, setupCommands, goCommands, exitCondition, postRunCommands, postExperimentCommands,
        timeLimit)

  override def isResizable: Boolean =
    true

  override def getMaximumSize: Dimension =
    new Dimension(super.getMaximumSize.width, Int.MaxValue)

  override def requestFocus(): Unit = {
    name.requestFocus()
  }
}
