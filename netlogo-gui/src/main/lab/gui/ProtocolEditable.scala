// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import org.nlogo.api.{ LabProtocol, LabVariableParser }
import org.nlogo.core.I18N
import org.nlogo.api.{ LabProtocol, RefValueSet }
import java.awt.{ GridBagConstraints, Window }
import org.nlogo.api.{ CompilerServices, Editable, Property }

// normally we'd be package-private but the org.nlogo.properties stuff requires we be public - ST 2/25/09

class ProtocolEditable(protocol: LabProtocol,
                       window: Window,
                       compiler: CompilerServices,
                       worldLock: AnyRef,
                       experimentNames: Seq[String] = Seq[String]())
  extends Editable {
  // these are for Editable
  def helpLink = Some("behaviorspace.html#creating-an-experiment-setup")
  val classDisplayName = "Experiment"
  def error(key:Object) = null
  def error(key:Object, e: Exception){}
  def anyErrors = false
  val sourceOffset = 0

  private implicit val i18nPrefix = I18N.Prefix("tools.behaviorSpace")

  val propertySet = {
    import scala.collection.JavaConverters._
    List(Property("hint", Property.Label, "<html>"+I18N.gui("hint")+"</html>",
                  backgroundColor = new java.awt.Color(128, 200, 128, 64), // de york green (light yellow-ish green)
                  borderSize = 6),
         Property("name", Property.String, I18N.gui("experimentName"),
                  "<html>"+I18N.gui("experimentName.info")+"</html>"),
         Property("valueSets", Property.ReporterOrEmpty,
                  I18N.gui("vary"), "<html>"+I18N.gui("vary.info")+"</html>"),
         Property("repetitions", Property.Integer, I18N.gui("repetitions"),
                  "<html>"+I18N.gui("repetitions.info")+"</html>"),
         Property("sequentialRunOrder", Property.Boolean, I18N.gui("sequentialRunOrder"),
                  "<html>"+ I18N.gui("sequentialRunOrder.info") +"</html>"),
         Property("metrics", Property.ReporterOrEmpty,
                  I18N.gui("metrics"),
                  "<html>"+I18N.gui("metrics.info")+"</html>"),
         Property("runMetricsEveryStep", Property.MetricsBoolean, I18N.gui("runMetricsEveryStep"),
                  "<html>"+I18N.gui("runMetricsEveryStep.info")+"</html>"),
         Property("runMetricsCondition", Property.ReporterLine, I18N.gui("runMetricsCondition"),
                  "<html>"+I18N.gui("runMetricsCondition.info")+"</html>", optional = true, enabled = !protocol.runMetricsEveryStep),
         Property("preExperimentCommands", Property.Commands, I18N.gui("preExperimentCommands"),
                  "<html>"+I18N.gui("preExperimentCommands.info")+"</html>",
                  collapsible=true, collapseByDefault=true),
         Property("setupCommands", Property.ReporterOrEmpty, I18N.gui("setupCommands"),
                  "<html>"+I18N.gui("setupCommands.info")+"</html>",
                  gridWidth = GridBagConstraints.RELATIVE),
         Property("goCommands", Property.Commands, I18N.gui("goCommands"),
                  "<html>"+I18N.gui("goCommands.info")+"</html>"),
         Property("exitCondition", Property.ReporterOrEmpty, I18N.gui("exitCondition"),
                  "<html>"+I18N.gui("exitCondition.info")+"</html>",
                  gridWidth = GridBagConstraints.RELATIVE, collapsible=true, collapseByDefault=true),
         Property("postRunCommands", Property.Commands, I18N.gui("postRunCommands"),
                  "<html>"+I18N.gui("postRunCommands.info")+"</html>", collapsible=true, collapseByDefault=true),
         Property("postExperimentCommands", Property.Commands, I18N.gui("postExperimentCommands"),
                  "<html>"+I18N.gui("postExperimentCommands.info")+"</html>",
                  collapsible=true, collapseByDefault=true),
         Property("timeLimit", Property.Integer, I18N.gui("timeLimit"),
                  "<html>"+I18N.gui("timeLimit.info")+"</html>")).asJava
  }
  // These are the actual vars the user edits.  Before editing they are copied out of the
  // original LabProtocol; after editing a new LabProtocol is created.
  var hint = ""
  var name = protocol.name
  var preExperimentCommands = protocol.preExperimentCommands
  var setupCommands = protocol.setupCommands
  var goCommands = protocol.goCommands
  var postRunCommands = protocol.postRunCommands
  var postExperimentCommands = protocol.postExperimentCommands
  var repetitions = protocol.repetitions
  var sequentialRunOrder = protocol.sequentialRunOrder
  var runMetricsEveryStep = protocol.runMetricsEveryStep
  var runMetricsCondition = protocol.runMetricsCondition
  var timeLimit = protocol.timeLimit
  var exitCondition = protocol.exitCondition
  var metrics = protocol.metrics.mkString("\n")
  var valueSets = LabVariableParser.combineVariables(protocol.constants, protocol.subExperiments)
  val runsCompleted = protocol.runsCompleted
  // make a new LabProtocol based on what user entered
  def editFinished: Boolean = get.isDefined
  def get: Option[LabProtocol] = {
    def complain(message: String) {
      if (!java.awt.GraphicsEnvironment.isHeadless)
        javax.swing.JOptionPane.showMessageDialog(
          window, I18N.gui.getN("edit.behaviorSpace.invalidVarySpec", message),
         I18N.gui("invalid"), javax.swing.JOptionPane.ERROR_MESSAGE)
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

  override def invalidSettings: Seq[(String,String)] = {
    if (name.trim.isEmpty) {
      return Seq(I18N.gui.get("edit.behaviorSpace.variable")
        -> I18N.gui.get("edit.behaviorSpace.name.empty"))
    }
    if (experimentNames.contains(name.trim)) {
      return Seq(I18N.gui.get("edit.behaviorSpace.variable")
        -> I18N.gui.getN("edit.behaviorSpace.name.duplicate", name.trim))
    }
    LabVariableParser.parseVariables(valueSets, repetitions, worldLock, compiler) match {
      case (None, message: String) => return Seq(I18N.gui.get("edit.behaviorSpace.variable") -> message)
      case _ =>
    }
    Seq.empty[(String, String)]
  }
}
