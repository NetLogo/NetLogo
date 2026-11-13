// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.awt.Window

import org.nlogo.api.{ CompilerServices, GlobalsIdentifier, LabProtocol, LabVariableParser, RefValueSet }
import org.nlogo.core.{ CompilerException, LocaleContext, WorldDimensions, WorldDimensions3D }
import org.nlogo.editor.Colorizer
import org.nlogo.swing.OptionPane
import org.nlogo.window.{ DummyErrorHandler, Editable, EditPanel }

import scala.util.{ Success, Failure }

private [gui] class ProtocolEditable(protocol: LabProtocol,
                                     window: Window,
                                     compiler: CompilerServices & GlobalsIdentifier,
                                     colorizer: Colorizer,
                                     worldLock: AnyRef,
                                     currentDims: WorldDimensions,
                                     experimentNames: Seq[String] = Seq[String]())
  extends Editable with DummyErrorHandler {
  // these are for Editable
  def helpLink = Option(("behaviorspace", "creating-an-experiment-setup"))
  val classDisplayName = "Experiment"
  val sourceOffset = 0

  private var _valueSets = LabVariableParser.combineVariables(protocol.constants, protocol.subExperiments)

  override def editPanel: EditPanel = new ProtocolEditPanel(this, compiler, colorizer)

  def name: String = protocol.name
  def setName(s: String): Unit = {
    protocol.name = s.trim
  }

  def preExperimentCommands: String = protocol.preExperimentCommands
  def setPreExperimentCommands(s: String): Unit = {
    protocol.preExperimentCommands = s.trim
  }

  def setupCommands: String = protocol.setupCommands
  def setSetupCommands(s: String): Unit = {
    protocol.setupCommands = s.trim
  }

  def goCommands: String = protocol.goCommands
  def setGoCommands(s: String): Unit = {
    protocol.goCommands = s.trim
  }

  def postRunCommands: String = protocol.postRunCommands
  def setPostRunCommands(s: String): Unit = {
    protocol.postRunCommands = s.trim
  }

  def postExperimentCommands: String = protocol.postExperimentCommands
  def setPostExperimentCommands(s: String): Unit = {
    protocol.postExperimentCommands = s.trim
  }

  def repetitions: Int = protocol.repetitions
  def setRepetitions(i: Int): Unit = {
    protocol.repetitions = i
  }

  def sequentialRunOrder: Boolean = protocol.sequentialRunOrder
  def setSequentialRunOrder(b: Boolean): Unit = {
    protocol.sequentialRunOrder = b
  }

  def runMetricsEveryStep: Boolean = protocol.runMetricsEveryStep
  def setRunMetricsEveryStep(b: Boolean): Unit = {
    protocol.runMetricsEveryStep = b
  }

  def runMetricsCondition: String = protocol.runMetricsCondition
  def setRunMetricsCondition(s: String): Unit = {
    protocol.runMetricsCondition = s.trim
  }

  def timeLimit: Int = protocol.timeLimit
  def setTimeLimit(i: Int): Unit = {
    protocol.timeLimit = i
  }

  def exitCondition: String = protocol.exitCondition
  def setExitCondition(s: String): Unit = {
    protocol.exitCondition = s.trim
  }

  def metrics: String = protocol.metricsForSaving.mkString("\n")
  def setMetrics(s: String): Unit = {
    protocol.metricsForSaving = s.split("\n", 0).map(_.trim).filter(_.nonEmpty).toList
  }

  def valueSets: String = _valueSets
  def setValueSets(s: String): Unit = {
    _valueSets = s.trim
  }

  // make a new LabProtocol based on what user entered
  def editFinished(): Boolean = get.isDefined
  def get: Option[LabProtocol] = {
    def complain(message: String): Unit = {
      val error = loc"""|Invalid spec for varying variables. Error:\n
                        |$message"""

      new OptionPane(window, loc"Invalid", error, OptionPane.Options.Ok, OptionPane.Icons.error)
    }
    LabVariableParser.parseVariables(valueSets, repetitions, worldLock, compiler) match {
      case Success((constants, subExperiments)) =>
        protocol.constants = constants
        protocol.subExperiments = subExperiments

        Option(protocol)

      case Failure(t) =>
        complain(t.getMessage)
        None
    }
  }

  override def errorString: Option[String] = {
    if (name.trim.isEmpty) {
      Option(loc"Experiment name cannot be empty")
    } else if (name.contains('/') || name.contains('\\')) {
      Option(loc"""Experiment name cannot contain "/" or "\\"""")
    } else if (experimentNames.contains(name.trim)) {
      Option(loc"""An experiment with the name "${name.trim}" already exists""")
    } else {
      LabVariableParser.parseVariables(valueSets, repetitions, worldLock, compiler) match {
        case Success((constants, subExperiments)) =>
          checkWorldDimensions(constants, subExperiments).orElse(checkCodeEditors())

        case Failure(t) =>
          Option(loc"""Compiler error in variable specification: ${t.getMessage}""")
      }
    }
  }

  // ensure that all provided value combinations for world dimensions are valid. this is done here instead
  // of in LabVariableParser, because the condition can't be checked one variable at a time. (Isaac B 12/30/25)
  private def checkWorldDimensions(constants: List[RefValueSet],
                                   subExperiments: List[List[RefValueSet]]): Option[String] = {
    LabProtocol.refElementsFor(constants, subExperiments).flatMap { vars =>
      val newDims: WorldDimensions3D = vars.foldLeft(currentDims.get3D) {
        case (dims, (name, value: Double)) =>
          val lower = name.toLowerCase

          if (lower == "min-pxcor") {
            dims.copyThreeD(minPxcor = value.toInt)
          } else if (lower == "max-pxcor") {
            dims.copyThreeD(maxPxcor = value.toInt)
          } else if (lower == "min-pycor") {
            dims.copyThreeD(minPycor = value.toInt)
          } else if (lower == "max-pycor") {
            dims.copyThreeD(maxPycor = value.toInt)
          } else if (lower == "min-pzcor") {
            dims.copyThreeD(minPzcor = value.toInt)
          } else if (lower == "max-pzcor") {
            dims.copyThreeD(maxPzcor = value.toInt)
          } else {
            dims
          }

        case (dims, _) =>
          dims
      }

      invalidDims("MIN-PXCOR", "MAX-PXCOR", newDims.minPxcor, newDims.maxPxcor)
        .orElse(invalidDims("MIN-PYCOR", "MAX-PYCOR",  newDims.minPycor, newDims.maxPycor))
        .orElse(invalidDims("MIN-PZCOR", "MAX-PZCOR",  newDims.minPzcor, newDims.maxPzcor))
    }.nextOption
  }

  private def invalidDims(minName: String, maxName: String, min: Int, max: Int): Option[String] = {
    if (max < 0) {
      Option(loc"$maxName cannot be negative.")
    } else if (min > 0) {
      Option(loc"$minName cannot be positive.")
    } else {
      None
    }
  }

  private def checkCommand(name: String, text: String): Option[String] = {
    try {
      val trimmed = text.trim

      if (trimmed.nonEmpty && !trimmed.startsWith(";"))
        compiler.checkCommandSyntax(trimmed)

      None
    } catch {
      case e: CompilerException =>
        Option(loc"""Compiler error in $name: ${e.getMessage}""")
    }
  }

  private def checkReporter(name: String, text: String): Option[String] = {
    try {
      val trimmed = text.trim

      if (trimmed.nonEmpty && !trimmed.startsWith(";"))
        compiler.checkReporterSyntax(trimmed)

      None
    } catch {
      case e: CompilerException =>
        Option(loc"""Compiler error in $name: ${e.getMessage}""")
    }
  }

  private def checkCodeEditors(): Option[String] = {
    metrics.split("\n").foldLeft(Option.empty[String]) {
      case (None, m) => checkReporter(loc"metrics", m)
      case (e, _) => e
    }.orElse(checkReporter(loc"run metrics when", runMetricsCondition))
    .orElse(checkCommand(loc"pre experiment commands", preExperimentCommands))
    .orElse(checkCommand(loc"setup commands", setupCommands))
    .orElse(checkCommand(loc"go commands", goCommands))
    .orElse {
        val trimmed: Option[String] = exitCondition.split("\n").dropWhile(_.trim.startsWith(";"))
                                        .dropWhile(_.trim.isEmpty).headOption

        trimmed.flatMap(checkReporter(loc"stop condition", _))
      }
    .orElse(checkCommand(loc"post run commands", postRunCommands))
    .orElse(checkCommand(loc"post experiment commands", postExperimentCommands))
  }
}
