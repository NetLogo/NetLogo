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
  def helpLink = Some("behaviorspace.html#creating-an-experiment-setup")
  val classDisplayName = "Experiment"
  def error(key:Object) = null
  def error(key:Object, e: Exception){}
  def anyErrors = false
  val sourceOffset = 0

  private implicit val i18nPrefix = I18N.Prefix("tools.behaviorSpace")

  val propertySet = {
    import scala.collection.JavaConverters._
    List(Property("hint", Property.Label, I18N.gui("hint")),
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
         Property("runMetricsEveryStep", Property.MetricsBoolean, I18N.gui("runMetricsEveryStep")),
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
          window, I18N.gui.getN("edit.behaviorSpace.invalidVarySpec", message),
         I18N.gui("invalid"), javax.swing.JOptionPane.ERROR_MESSAGE)
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
              if (constants.exists(_.variableName == variableName)) {
                complain(I18N.gui.getN("edit.behaviorSpace.constantDefinedTwice", variableName)); return None
              }
              if (!subExperiments.isEmpty) {
                complain(I18N.gui.getN("edit.behaviorSpace.constantDefinedSubexperiment", variableName)); return None
              }
              constants = constants :+ constant
            case _ =>
              complain(I18N.gui.getN("edit.behaviorSpace.expectedThreeNumbers", Dump.list(more))); return None
          }
        case List(variableName: String, more@_*) =>
          if (more.isEmpty) {complain(I18N.gui.getN("edit.behaviorSpace.expectedValue", variableName)); return None}
          val constant = new RefEnumeratedValueSet(variableName, more.toList)
          if (constants.exists(_.variableName == variableName)) {
            complain(I18N.gui.getN("edit.behaviorSpace.constantDefinedTwice", variableName)); return None
          }
          if (!subExperiments.isEmpty) {
            complain(I18N.gui.getN("edit.behaviorSpace.constantDefinedSubexperiment", variableName)); return None
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
                  if (subExperiment.exists(_.variableName == variableName)) {
                    complain(I18N.gui.getN("edit.behaviorSpace.variableDefinedTwiceSubexperiment",
                      variableName)); return None
                  }
                  subExperiment = subExperiment :+ exp
                case _ =>
                  complain(I18N.gui.getN("edit.behaviorSpace.expectedThreeNumbers", Dump.list(more))); return None
              }
            case List(variableName: String, more@_*) =>
              if (more.isEmpty) {complain(I18N.gui.getN("edit.behaviorSpace.expectedValue", variableName)); return None}
              val exp = new RefEnumeratedValueSet(variableName, more.toList)
              if (subExperiment.exists(_.variableName == variableName)) {
                complain(I18N.gui.getN("edit.behaviorSpace.variableDefinedTwiceSubexperiment", variableName)); return None
              }
              subExperiment = subExperiment :+ exp
            case _ =>
              complain(I18N.gui.get("edit.behaviorSpace.invalidFormat")); return None
          })
          subExperiments = subExperiments :+ subExperiment
        case _ =>
          complain(I18N.gui.get("edit.behaviorSpace.invalidFormat")); return None
      }
    }
    for (experiment <- subExperiments) {
      for (valueSet <- experiment) {
        if (!constants.exists(_.variableName == valueSet.variableName) &&
            subExperiments.exists(!_.exists(_.variableName == valueSet.variableName))) {
          complain(I18N.gui.getN("edit.behaviorSpace.constantDefinedSubexperiment", valueSet.variableName)); return None
        }
      }
    }
    Some(new LabProtocol(
      name.trim, preExperimentCommands.trim, setupCommands.trim, goCommands.trim,
      postRunCommands.trim, postExperimentCommands.trim, repetitions, sequentialRunOrder, runMetricsEveryStep,
      runMetricsCondition.trim, timeLimit, exitCondition.trim,
      metrics.split("\n", 0).map(_.trim).filter(!_.isEmpty).toList,
      constants, subExperiments, runsCompleted))
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
    val list =
        try { worldLock.synchronized {
          compiler.readFromString("[" + valueSets + "]").asInstanceOf[LogoList]
        } }
      catch{ case ex: CompilerException =>  return Seq(I18N.gui.get("edit.behaviorSpace.variable") ->
        I18N.gui.getN("edit.behaviorSpace.compiler.parser")) }
    var totalCombinations = 1
    list.toList.foreach {
      case element =>
        element.asInstanceOf[LogoList].toList match {
          case List() => return Seq(I18N.gui.get("edit.behaviorSpace.variable") ->
            I18N.gui.getN("edit.behaviorSpace.list.field"))
          case List(variableName: String, more: LogoList) =>
            more.toList match {
              case List(first: java.lang.Double,
                        step: java.lang.Double,
                        last: java.lang.Double) =>
                if (((last > first && step > 0) || (last < first && step < 0)) &&
                    Int.MaxValue / totalCombinations > ((last - first) / step + 1)){
                  val multiplier: Int = ((last - first) / step + 1).toInt
                  totalCombinations = totalCombinations * (if (multiplier == 0) 1 else multiplier)
                } else
                  return Seq(I18N.gui.get("edit.behaviorSpace.variable") ->
                             I18N.gui.getN("edit.behaviorSpace.list.increment",
                                           s"[ ${'"' + variableName + '"'} [ $first $step $last ] ]"))
              case _ =>
                return Seq(I18N.gui.get("edit.behaviorSpace.variable") ->
                  I18N.gui.getN("edit.behaviorSpace.list.incrementinvalid", variableName))
            }
          case List(variableName: String, more@_*) =>
            if (more.isEmpty){
              return Seq(I18N.gui.get("edit.behaviorSpace.variable") ->
                I18N.gui.getN("edit.behaviorSpace.list.field", variableName))
            }
            if ( Int.MaxValue / totalCombinations > more.toList.size )
              totalCombinations = totalCombinations * more.toList.size
            else return Seq(I18N.gui.get("edit.behaviorSpace.variable") ->
              I18N.gui.getN("edit.behaviorSpace.list.variablelist", variableName))
          case List(first: LogoList, more@_*) =>
            (List(first) ++ more).foreach(_.asInstanceOf[LogoList].toList match {
              case List() => return Seq(I18N.gui.get("edit.behaviorSpace.variable") ->
                                        I18N.gui.getN("edit.behaviorSpace.list.field"))
              case List(variableName: String, more: LogoList) =>
                more.toList match {
                  case List(first: java.lang.Double,
                            step: java.lang.Double,
                            last: java.lang.Double) =>
                    if (((last > first && step > 0) || (last < first && step < 0)) &&
                        Int.MaxValue / totalCombinations > ((last - first) / step + 1)){
                      val multiplier: Int = ((last - first) / step + 1).toInt
                      totalCombinations = totalCombinations * (if (multiplier == 0) 1 else multiplier)
                    } else
                      return Seq(I18N.gui.get("edit.behaviorSpace.variable") ->
                                 I18N.gui.getN("edit.behaviorSpace.list.increment",
                                               s"[ ${'"' + variableName + '"'} [ $first $step $last ] ]"))
                  case _ =>
                    return Seq(I18N.gui.get("edit.behaviorSpace.variable") ->
                               I18N.gui.getN("edit.behaviorSpace.list.incrementinvalid", variableName))
                }
              case List(variableName: String, more@_*) =>
                if (more.isEmpty){
                  return Seq(I18N.gui.get("edit.behaviorSpace.variable") ->
                             I18N.gui.getN("edit.behaviorSpace.list.field", variableName))
                }
                if ( Int.MaxValue / totalCombinations > more.toList.size )
                  totalCombinations = totalCombinations * more.toList.size
                else return Seq(I18N.gui.get("edit.behaviorSpace.variable") ->
                                I18N.gui.getN("edit.behaviorSpace.list.variablelist", variableName))
                })
          case _ => return Seq(I18N.gui.get("edit.behaviorSpace.variable") ->
                               I18N.gui.getN("edit.behaviorSpace.list.unexpected"))
        }
    }
    if ( repetitions > 0 && Int.MaxValue / repetitions >= totalCombinations )
      Seq.empty[(String,String)]
    else
      Seq(I18N.gui.get("edit.behaviorSpace.variable")
        -> I18N.gui.getN("edit.behaviorSpace.repetition.totalrun"))
  }
}
