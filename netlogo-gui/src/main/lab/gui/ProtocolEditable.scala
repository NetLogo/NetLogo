// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.awt.Window

import org.nlogo.api.{ CompilerServices, GlobalsIdentifier, LabProtocol, LabVariableParser }
import org.nlogo.core.{ CompilerException, I18N }
import org.nlogo.editor.Colorizer
import org.nlogo.swing.OptionPane
import org.nlogo.window.{ DummyErrorHandler, Editable, EditPanel }

import scala.util.{ Success, Failure }

private [gui] class ProtocolEditable(protocol: LabProtocol,
                                     window: Window,
                                     compiler: CompilerServices & GlobalsIdentifier,
                                     colorizer: Colorizer,
                                     worldLock: AnyRef,
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

  def metrics: String = protocol.metrics.mkString("\n")
  def setMetrics(s: String): Unit = {
    protocol.metrics = s.split("\n", 0).map(_.trim).filter(_.nonEmpty).toList
  }

  def valueSets: String = _valueSets
  def setValueSets(s: String): Unit = {
    _valueSets = s.trim
  }

  // make a new LabProtocol based on what user entered
  def editFinished(): Boolean = get.isDefined
  def get: Option[LabProtocol] = {
    def complain(message: String): Unit = {
      if (!java.awt.GraphicsEnvironment.isHeadless) {
        new OptionPane(window, I18N.gui("invalid"), I18N.gui.getN("edit.behaviorSpace.invalidVarySpec", message),
                       OptionPane.Options.Ok, OptionPane.Icons.Error)
      }
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
        case Failure(t) =>
          return Some(I18N.gui.getN("edit.behaviorSpace.compilerError",
                                    I18N.gui.get("edit.behaviorSpace.variableSpec"), t.getMessage))

        case _ =>
      }

      def checkCommand(name: String, text: String): Option[String] = {
        try {
          if (text.trim.nonEmpty)
            compiler.checkCommandSyntax(text.trim)
        } catch {
          case e: CompilerException =>
            return Some(I18N.gui.getN("edit.behaviorSpace.compilerError", name, e.getMessage))
        }

        None
      }

      def checkReporter(name: String, text: String): Option[String] = {
        try {
          if (text.trim.nonEmpty)
            compiler.checkReporterSyntax(text.trim)
        } catch {
          case e: CompilerException =>
            return Some(I18N.gui.getN("edit.behaviorSpace.compilerError", name, e.getMessage))
        }

        None
      }

      metrics.split("\n").foldLeft(Option.empty[String]) {
        case (None, m) => checkReporter(I18N.gui.get("edit.behaviorSpace.metrics"), m)
        case (e, _) => e
      }.orElse(checkReporter(I18N.gui.get("edit.behaviorSpace.runMetricsCondition"), runMetricsCondition))
       .orElse(checkCommand(I18N.gui.get("edit.behaviorSpace.preExperimentCommands"), preExperimentCommands))
       .orElse(checkCommand(I18N.gui.get("edit.behaviorSpace.setupCommands"), setupCommands))
       .orElse(checkCommand(I18N.gui.get("edit.behaviorSpace.goCommands"), goCommands))
       .orElse(checkReporter(I18N.gui.get("edit.behaviorSpace.exitCondition"), exitCondition))
       .orElse(checkCommand(I18N.gui.get("edit.behaviorSpace.postRunCommands"), postRunCommands))
       .orElse(checkCommand(I18N.gui.get("edit.behaviorSpace.postExperimentCommands"), postExperimentCommands))
    }
  }
}
