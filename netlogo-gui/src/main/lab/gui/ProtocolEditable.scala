// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab.gui

import org.nlogo.api.LabProtocol
import org.nlogo.core.{ CompilerException, I18N, LogoList }
import org.nlogo.api.{ EnumeratedValueSet, LabProtocol, RefEnumeratedValueSet, SteppedValueSet, RefValueSet }
import java.awt.{ GridBagConstraints, Window }
import org.nlogo.api.{ Dump, CompilerServices, Editable, Property }

// normally we'd be package-private but the org.nlogo.properties stuff requires we be public - ST 2/25/09

class ProtocolEditable(protocol: LabProtocol,
                       window: Window,
                       compiler: CompilerServices,
                       worldLock: AnyRef,
                       experimentNames: Seq[String] = Seq[String]())
  extends Editable {
  // these are for Editable
  def helpLink = None
  val classDisplayName = "Experiment"
  def error(key:Object) = null
  def error(key:Object, e: Exception){}
  def anyErrors = false
  val sourceOffset = 0

  private implicit val i18nPrefix = I18N.Prefix("tools.behaviorSpace")

  val propertySet = {
    import scala.collection.JavaConverters._
    List(Property("name", Property.String, I18N.gui("experimentName")),
         Property("valueSets", Property.ReporterOrEmpty,
                  I18N.gui("vary"), "<html>"+I18N.gui("vary.info")+"</html>"),
         Property("repetitions", Property.Integer, I18N.gui("repetitions"),
                  "<html>"+I18N.gui("repetitions.info")+"</html>"),
         Property("sequentialRunOrder", Property.Boolean, I18N.gui("sequentialRunOrder"),
                  "<html>"+ I18N.gui("sequentialRunOrder.info") +"</html>"),
         Property("metrics", Property.ReporterOrEmpty,
                  I18N.gui("metrics"),
                  "<html>"+I18N.gui("metrics.info")+"</html>"),
         Property("runMetricsEveryStep", Property.MetricsBoolean, I18N.gui("runMetricsEveryStep")),
         Property("runMetricsCondition", Property.ReporterLine, I18N.gui("runMetricsCondition"),
                  "<html>"+I18N.gui("runMetricsCondition.info")+"</html>", optional = true, enabled = !protocol.runMetricsEveryStep),
         Property("setupCommands", Property.ReporterOrEmpty, I18N.gui("setupCommands"),
                  gridWidth = GridBagConstraints.RELATIVE),
         Property("goCommands", Property.Commands, I18N.gui("goCommands")),
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
  var name = protocol.name
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
  var valueSets = {
    def setString(valueSet: RefValueSet) =
      "[\"" + valueSet.variableName + "\" " +
      (valueSet match {
         case evs: EnumeratedValueSet =>
           evs.map(x => Dump.logoObject(x.asInstanceOf[AnyRef], true, false)).mkString(" ")
         case evs: RefEnumeratedValueSet =>
           evs.map(x => Dump.logoObject(x.asInstanceOf[AnyRef], true, false)).mkString(" ")
         case svs: SteppedValueSet =>
           List(svs.firstValue, svs.step, svs.lastValue).map(_.toString).mkString("[", " ", "]")
       }) + "]"
    (protocol.constants.map(setString) :::
     protocol.subExperiments.map("[" + _.map(setString).mkString + "]")).mkString("\n")
  }
  val runsCompleted = protocol.runsCompleted
  // make a new LabProtocol based on what user entered
  def editFinished: Boolean = get.isDefined
  def get: Option[LabProtocol] = {
    def complain(message: String) {
      if (!java.awt.GraphicsEnvironment.isHeadless)
        javax.swing.JOptionPane.showMessageDialog(
          window, "Invalid spec for varying variables. Error:\n" + message,
         "Invalid", javax.swing.JOptionPane.ERROR_MESSAGE)
    }
    val list =
      try { worldLock.synchronized {
        compiler.readFromString("[" + valueSets + "]").asInstanceOf[LogoList]
      } }
    catch{ case ex: CompilerException => complain(ex.getMessage); return None }
    var constants = List[RefValueSet]()
    var subExperiments = List[List[RefValueSet]]()
    for (o <- list.toList) {
      o.asInstanceOf[LogoList].toList match {
        case List(variableName: String, more: LogoList) =>
          more.toList match {
            case List(first: java.lang.Double,
                      step: java.lang.Double,
                      last: java.lang.Double) =>
              val constant = new SteppedValueSet(variableName,
                                                 BigDecimal(Dump.number(first)),
                                                 BigDecimal(Dump.number(step)),
                                                 BigDecimal(Dump.number(last)))
              if (constants.contains(constant)) {
                complain(s"Constant ${variableName} defined twice"); return None
              }
              if (!subExperiments.isEmpty) {
                complain(s"Constant ${variableName} defined after subexperiment"); return None
              }
              constants = constants :+ constant
            case _ =>
              complain("Expected three numbers here: " + Dump.list(more)); return None
          }
        case List(variableName: String, more@_*) =>
          if (more.isEmpty) {complain(s"Expected a value for variable $variableName"); return None}
          val constant = new RefEnumeratedValueSet(variableName, more.toList)
          if (constants.contains(constant)) {
            complain(s"Constant ${variableName} defined twice"); return None
          }
          if (!subExperiments.isEmpty) {
            complain(s"Constant ${variableName} defined after subexperiment"); return None
          }
          constants = constants :+ constant
        case List(first: LogoList, more@_*) =>
          var subExperiment = List[RefValueSet]()
          (List(first) ++ more).foreach(_.asInstanceOf[LogoList].toList match {
            case List(variableName: String, more: LogoList) =>
              more.toList match {
                case List(first: java.lang.Double,
                          step: java.lang.Double,
                          last: java.lang.Double) =>
                  val exp = new SteppedValueSet(variableName,
                                                BigDecimal(Dump.number(first)),
                                                BigDecimal(Dump.number(step)),
                                                BigDecimal(Dump.number(last)))
                  if (subExperiment.contains(exp)) {
                    complain(s"Variable ${variableName} defined twice in one subexperiment"); return None
                  }
                  subExperiment = subExperiment :+ exp
                case _ =>
                  complain("Expected three numbers here: " + Dump.list(more)); return None
              }
            case List(variableName: String, more@_*) =>
              if (more.isEmpty) {complain(s"Expected a value for variable $variableName"); return None}
              val exp = new RefEnumeratedValueSet(variableName, more.toList)
              if (subExperiment.contains(exp)) {
                complain(s"Variable ${variableName} defined twice in one subexperiment"); return None
              }
              subExperiment = subExperiment :+ exp
            case _ =>
              complain("Invalid format" + (List(first.toList) ++ more)); return None
          })
          subExperiments = subExperiments :+ subExperiment
        case _ =>
          complain("Invalid format"); return None
      }
    }
    for (experiment <- subExperiments) {
      for (valueSet <- experiment) {
        if (!constants.exists(_.variableName == valueSet.variableName) &&
            subExperiments.exists(!_.exists(_.variableName == valueSet.variableName))) {
          complain(s"Variable ${valueSet.variableName} must be defined as a constant" +
                    " if not defined for all subexperiments"); return None
        }
      }
    }
    Some(new LabProtocol(
      name.trim, setupCommands.trim, goCommands.trim,
      postRunCommands.trim, postExperimentCommands.trim, repetitions, sequentialRunOrder, runMetricsEveryStep,
      runMetricsCondition.trim, timeLimit, exitCondition.trim,
      metrics.split("\n", 0).map(_.trim).filter(!_.isEmpty).toList,
      constants, subExperiments, runsCompleted))
  }

  override def invalidSettings: Seq[(String,String)] = {
    if (name.trim.isEmpty) {
      return Seq("Variable" -> I18N.gui.get("edit.behaviorSpace.name.empty"))
    }
    if (experimentNames.contains(name.trim)) {
      return Seq("Variable" -> I18N.gui.getN("edit.behaviorSpace.name.duplicate", name.trim))
    }
    val list =
        try { worldLock.synchronized {
          compiler.readFromString("[" + valueSets + "]").asInstanceOf[LogoList]
        } }
      catch{ case ex: CompilerException =>  return Seq("Variable" -> I18N.gui.getN("edit.behaviorSpace.compiler.parser")) }
    var totalCombinations = 1
    list.toList.foreach {
      case element =>
        element.asInstanceOf[LogoList].toList match {
          case List() => return Seq("Variable" -> I18N.gui.getN("edit.behaviorSpace.list.field"))
          case List(variableName: String, more: LogoList) =>
            more.toList match {
              case List(first: java.lang.Double,
                        step: java.lang.Double,
                        last: java.lang.Double)
                  if last > first && step > 0  =>
                if (Int.MaxValue / totalCombinations > ((last - first) / step + 1)){
                  val multiplier: Int = ((last - first) / step + 1).toInt
                  totalCombinations = totalCombinations * (if (multiplier == 0) 1 else multiplier)
                } else
                  return Seq("Variable" -> I18N.gui.getN("edit.behaviorSpace.list.increment", variableName, s"[ $first $step $last ]"))
              case _ =>
                return Seq("Variable" -> I18N.gui.getN("edit.behaviorSpace.list.incrementinvalid", variableName))
            }
          case List(variableName: String, more@_*) =>
            if (more.isEmpty){
              return Seq("Variable" -> I18N.gui.getN("edit.behaviorSpace.list.field", variableName))
            }
            if ( Int.MaxValue / totalCombinations > more.toList.size )
              totalCombinations = totalCombinations * more.toList.size
            else return Seq("Variable" -> I18N.gui.getN("edit.behaviorSpace.list.variablelist", variableName))
          case List(first: LogoList, more@_*) =>
          case _ => return Seq("Variable" -> I18N.gui.getN("edit.behaviorSpace.list.unexpected"))
        }
    }
    if ( repetitions > 0 && Int.MaxValue / repetitions >= totalCombinations )
      Seq.empty[(String,String)]
    else
      Seq("Variable" -> I18N.gui.getN("edit.behaviorSpace.repetition.totalrun"))
  }
}
