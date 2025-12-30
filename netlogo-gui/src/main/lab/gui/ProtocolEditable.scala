// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.awt.Window

import org.nlogo.api.{ CompilerServices, GlobalsIdentifier, LabProtocol, LabVariableParser, RefValueSet }
import org.nlogo.core.{ CompilerException, I18N, WorldDimensions, WorldDimensions3D }
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
  def helpLink = Some("behaviorspace.html#creating-an-experiment-setup")
  val classDisplayName = "Experiment"
  val sourceOffset = 0

  private implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("tools.behaviorSpace")

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
      new OptionPane(window, I18N.gui("invalid"), I18N.gui.getN("edit.behaviorSpace.invalidVarySpec", message),
                     OptionPane.Options.Ok, OptionPane.Icons.Error)
    }
    LabVariableParser.parseVariables(valueSets, repetitions, worldLock, compiler) match {
      case Success((constants, subExperiments)) =>
        protocol.constants = constants
        protocol.subExperiments = subExperiments

        Some(protocol)

      case Failure(t) =>
        complain(t.getMessage)
        None
    }
  }

  override def errorString: Option[String] = {
    if (name.trim.isEmpty) {
      Some(I18N.gui.get("edit.behaviorSpace.name.empty"))
    } else if (name.contains('/') || name.contains('\\')) {
      Some(I18N.gui.get("edit.behaviorSpace.name.slashes"))
    } else if (experimentNames.contains(name.trim)) {
      Some(I18N.gui.getN("edit.behaviorSpace.name.duplicate", name.trim))
    } else {
      LabVariableParser.parseVariables(valueSets, repetitions, worldLock, compiler) match {
        case Success((constants, subExperiments)) =>
          checkWorldDimensions(constants, subExperiments).orElse(checkCodeEditors())

        case Failure(t) =>
          Some(I18N.gui.getN("edit.behaviorSpace.compilerError", I18N.gui.get("edit.behaviorSpace.variableSpec"),
                             t.getMessage))
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

      if (invalidDims(newDims.minPxcor, newDims.maxPxcor)) {
        Some(I18N.gui.get("edit.behaviorSpace.invalidDimsX"))
      } else if (invalidDims(newDims.minPycor, newDims.maxPycor)) {
        Some(I18N.gui.get("edit.behaviorSpace.invalidDimsY"))
      } else if (invalidDims(newDims.minPzcor, newDims.maxPzcor)) {
        Some(I18N.gui.get("edit.behaviorSpace.invalidDimsZ"))
      } else {
        None
      }
    }.nextOption
  }

  private def invalidDims(min: Int, max: Int): Boolean =
    max < min || (min < 0 && max < 0) || (min > 0 && max > 0)

  private def checkCommand(name: String, text: String): Option[String] = {
    try {
      val trimmed = text.trim

      if (trimmed.nonEmpty && !trimmed.startsWith(";"))
        compiler.checkCommandSyntax(trimmed)

      None
    } catch {
      case e: CompilerException =>
        Some(I18N.gui.getN("edit.behaviorSpace.compilerError", name, e.getMessage))
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
        Some(I18N.gui.getN("edit.behaviorSpace.compilerError", name, e.getMessage))
    }
  }

  private def checkCodeEditors(): Option[String] = {
    metrics.split("\n").foldLeft(Option.empty[String]) {
      case (None, m) => checkReporter(I18N.gui.get("edit.behaviorSpace.metrics"), m)
      case (e, _) => e
    }.orElse(checkReporter(I18N.gui.get("edit.behaviorSpace.runMetricsCondition"), runMetricsCondition))
    .orElse(checkCommand(I18N.gui.get("edit.behaviorSpace.preExperimentCommands"), preExperimentCommands))
    .orElse(checkCommand(I18N.gui.get("edit.behaviorSpace.setupCommands"), setupCommands))
    .orElse(checkCommand(I18N.gui.get("edit.behaviorSpace.goCommands"), goCommands))
    .orElse {
        val trimmed: Option[String] = exitCondition.split("\n").dropWhile(_.trim.startsWith(";"))
                                        .dropWhile(_.trim.isEmpty).headOption

        trimmed.flatMap(checkReporter(I18N.gui.get("edit.behaviorSpace.exitCondition"), _))
      }
    .orElse(checkCommand(I18N.gui.get("edit.behaviorSpace.postRunCommands"), postRunCommands))
    .orElse(checkCommand(I18N.gui.get("edit.behaviorSpace.postExperimentCommands"), postExperimentCommands))
  }
}
