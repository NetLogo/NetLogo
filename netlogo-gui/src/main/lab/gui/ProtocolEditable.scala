// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import java.awt.Window

import org.nlogo.api.{ CompilerServices, LabProtocol, LabVariableParser, RefValueSet }
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.swing.OptionPane
import org.nlogo.window.{ DummyErrorHandler, Editable, EditPanel }

// normally we'd be package-private but the org.nlogo.properties stuff requires we be public - ST 2/25/09

class ProtocolEditable(protocol: LabProtocol,
                       window: Window,
                       compiler: CompilerServices,
                       colorizer: Colorizer,
                       worldLock: AnyRef,
                       experimentNames: Seq[String] = Seq[String]())
  extends Editable with DummyErrorHandler {
  // these are for Editable
  def helpLink = Some("behaviorspace.html#creating-an-experiment-setup")
  val classDisplayName = "Experiment"
  val sourceOffset = 0

  private implicit val i18nPrefix = I18N.Prefix("tools.behaviorSpace")

  override def editPanel: EditPanel = new ProtocolEditPanel(this, compiler, colorizer)

  // These are the actual vars the user edits.  Before editing they are copied out of the
  // original LabProtocol; after editing a new LabProtocol is created.
  private var _name = protocol.name
  private var _preExperimentCommands = protocol.preExperimentCommands
  private var _setupCommands = protocol.setupCommands
  private var _goCommands = protocol.goCommands
  private var _postRunCommands = protocol.postRunCommands
  private var _postExperimentCommands = protocol.postExperimentCommands
  private var _repetitions = protocol.repetitions
  private var _sequentialRunOrder = protocol.sequentialRunOrder
  private var _runMetricsEveryStep = protocol.runMetricsEveryStep
  private var _runMetricsCondition = protocol.runMetricsCondition
  private var _timeLimit = protocol.timeLimit
  private var _exitCondition = protocol.exitCondition
  private var _metrics = protocol.metrics.mkString("\n")
  private var _valueSets = LabVariableParser.combineVariables(protocol.constants, protocol.subExperiments)

  val runsCompleted = protocol.runsCompleted

  def name: String = _name
  def setName(s: String): Unit = {
    _name = s
  }

  def preExperimentCommands: String = _preExperimentCommands
  def setPreExperimentCommands(s: String): Unit = {
    _preExperimentCommands = s
  }

  def setupCommands: String = _setupCommands
  def setSetupCommands(s: String): Unit = {
    _setupCommands = s
  }

  def goCommands: String = _goCommands
  def setGoCommands(s: String): Unit = {
    _goCommands = s
  }

  def postRunCommands: String = _postRunCommands
  def setPostRunCommands(s: String): Unit = {
    _postRunCommands = s
  }

  def postExperimentCommands: String = _postExperimentCommands
  def setPostExperimentCommands(s: String): Unit = {
    _postExperimentCommands = s
  }

  def repetitions: Int = _repetitions
  def setRepetitions(i: Int): Unit = {
    _repetitions = i
  }

  def sequentialRunOrder: Boolean = _sequentialRunOrder
  def setSequentialRunOrder(b: Boolean): Unit = {
    _sequentialRunOrder = b
  }

  def runMetricsEveryStep: Boolean = _runMetricsEveryStep
  def setRunMetricsEveryStep(b: Boolean): Unit = {
    _runMetricsEveryStep = b
  }

  def runMetricsCondition: String = _runMetricsCondition
  def setRunMetricsCondition(s: String): Unit = {
    _runMetricsCondition = s
  }

  def timeLimit: Int = _timeLimit
  def setTimeLimit(i: Int): Unit = {
    _timeLimit = i
  }

  def exitCondition: String = _exitCondition
  def setExitCondition(s: String): Unit = {
    _exitCondition = s
  }

  def metrics: String = _metrics
  def setMetrics(s: String): Unit = {
    _metrics = s
  }

  def valueSets: String = _valueSets
  def setValueSets(s: String): Unit = {
    _valueSets = s
  }

  // make a new LabProtocol based on what user entered
  def editFinished: Boolean = get.isDefined
  def get: Option[LabProtocol] = {
    def complain(message: String) {
      if (!java.awt.GraphicsEnvironment.isHeadless) {
        new OptionPane(window, I18N.gui("invalid"), I18N.gui.getN("edit.behaviorSpace.invalidVarySpec", message),
                       OptionPane.Options.Ok, OptionPane.Icons.Error)
      }
    }
    return LabVariableParser.parseVariables(valueSets, repetitions, worldLock, compiler) match {
      case (Some((constants: List[RefValueSet], subExperiments: List[List[RefValueSet]])), _) =>
        Some(new LabProtocol(
          name.trim, preExperimentCommands.trim, setupCommands.trim, goCommands.trim,
          postRunCommands.trim, postExperimentCommands.trim, repetitions, sequentialRunOrder, runMetricsEveryStep,
          runMetricsCondition.trim, timeLimit, exitCondition.trim,
          metrics.split("\n", 0).map(_.trim).filter(!_.isEmpty).toList,
          constants, subExperiments, runsCompleted))
      case (None, message: String) =>
        complain(message)
        None
    }
  }

  override def errorString: Option[String] = {
    if (name.trim.isEmpty) {
      Some(I18N.gui.get("edit.behaviorSpace.name.empty"))
    } else if (experimentNames.contains(name.trim)) {
      Some(I18N.gui.getN("edit.behaviorSpace.name.duplicate", name.trim))
    } else {
      LabVariableParser.parseVariables(valueSets, repetitions, worldLock, compiler) match {
        case (None, message: String) => Some(message)
        case _ => None
      }
    }
  }
}
