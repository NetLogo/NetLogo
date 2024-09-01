// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ LiteralParser, XMLElement }

import scala.collection.mutable.Set

object LabXMLLoader {
  def readExperiment(element: XMLElement, literalParser: LiteralParser, editNames: Boolean,
                     existingNames: Set[String]): LabProtocol = {
    def readValueSet(element: XMLElement): RefValueSet = {
      element.name match {
        case "steppedValueSet" =>
          SteppedValueSet(element("variable"), element("first").toDouble, element("step").toDouble,
                          element("last").toDouble)

        case "enumeratedValueSet" =>
          RefEnumeratedValueSet(element("variable"), for (element <- element.children if element.name == "value")
                                                       yield literalParser.readFromString(element("value")))

      }
    }

    var preExperiment = ""
    var setup = ""
    var go = ""
    var postRun = ""
    var postExperiment = ""
    var runMetricsCondition = ""
    var exitCondition = ""
    var metrics = List[String]()
    var constants = List[RefValueSet]()
    var subExperiments = List[List[RefValueSet]]()

    for (element <- element.children) {
      element.name match {
        case "preExperiment" =>
          preExperiment = element.text
        
        case "setup" =>
          setup = element.text
        
        case "go" =>
          go = element.text
        
        case "postRun" =>
          postRun = element.text
        
        case "postExperiment" =>
          postExperiment = element.text
        
        case "runMetricsCondition" =>
          runMetricsCondition = element.text
        
        case "exitCondition" =>
          exitCondition = element.text
        
        case "metrics" =>
          metrics = for (element <- element.children if element.name == "metric") yield element.text
        
        case "constants" =>
          constants = element.children.map(readValueSet)
        
        case "subExperiments" =>
          subExperiments = for (element <- element.children if element.name == "subExperiment")
                              yield element.children.map(readValueSet)

      }
    }

    var name = element("name")

    if (editNames) {
      if (name.nonEmpty) {
        if (existingNames.contains(name)) {
          var n = 1

          while (existingNames.contains(s"$name ($n)")) n += 1

          name = s"$name ($n)"
        }
      }

      else if (existingNames.contains("no name")) {
        var n = 1

        while (existingNames.contains(s"no name ($n)")) n += 1

        name = s"no name ($n)"
      }

      else {
        name = "no name"
      }

      existingNames += name
    }

    LabProtocol(name, preExperiment, setup, go, postRun, postExperiment, element("repetitions").toInt,
                element("sequentialRunOrder").toBoolean, element("runMetricsEveryStep").toBoolean, runMetricsCondition,
                element("timeLimit", "0").toInt, exitCondition, metrics, constants, subExperiments)
  }

  def writeExperiment(experiment: LabProtocol): XMLElement = {
    def writeValueSet(valueSet: RefValueSet): XMLElement = {
      valueSet match {
        case stepped: SteppedValueSet =>
          val attributes = Map[String, String](
            ("variable", stepped.variableName),
            ("first", stepped.firstValue.toString),
            ("step", stepped.step.toString),
            ("last", stepped.lastValue.toString)
          )

          XMLElement("steppedValueSet", attributes, "", Nil)
        
        case enumerated: RefEnumeratedValueSet =>
          val attributes = Map[String, String](
            ("variable", enumerated.variableName)
          )

          val children =
            for (value <- enumerated.toList)
              yield XMLElement("value", Map(("value", Dump.logoObject(value, true, false))), "", Nil)

          XMLElement("enumeratedValueSet", attributes, "", children)
        
      }
    }

    var attributes = Map[String, String](
      ("name", experiment.name),
      ("repetitions", experiment.repetitions.toString),
      ("sequentialRunOrder", experiment.sequentialRunOrder.toString),
      ("runMetricsEveryStep", experiment.runMetricsEveryStep.toString)
    )

    if (experiment.timeLimit != 0)
      attributes += (("timeLimit", experiment.timeLimit.toString))

    var children = List[XMLElement]()

    if (experiment.preExperimentCommands.trim.nonEmpty)
      children = children :+ XMLElement("preExperiment", Map(), experiment.preExperimentCommands.trim, Nil)
    
    if (experiment.setupCommands.trim.nonEmpty)
      children = children :+ XMLElement("setup", Map(), experiment.setupCommands.trim, Nil)

    if (experiment.goCommands.trim.nonEmpty)
      children = children :+ XMLElement("go", Map(), experiment.goCommands.trim, Nil)

    if (experiment.postRunCommands.trim.nonEmpty)
      children = children :+ XMLElement("postRun", Map(), experiment.postRunCommands.trim, Nil)

    if (experiment.postExperimentCommands.trim.nonEmpty)
      children = children :+ XMLElement("postExperiment", Map(), experiment.postExperimentCommands.trim, Nil)

    if (experiment.exitCondition.trim.nonEmpty)
      children = children :+ XMLElement("exitCondition", Map(), experiment.exitCondition.trim, Nil)

    if (experiment.runMetricsCondition.trim.nonEmpty)
      children = children :+ XMLElement("runMetricsCondition", Map(), experiment.runMetricsCondition.trim, Nil)

    if (experiment.metrics.nonEmpty) {
      val metrics =
        for (metric <- experiment.metrics) yield {
          XMLElement("metric", Map(), metric, Nil)
        }

      children = children :+ XMLElement("metrics", Map(), "", metrics)
    }

    if (experiment.constants.nonEmpty)
      children = children :+ XMLElement("constants", Map(), "", experiment.constants.map(writeValueSet))

    for (subExperiment <- experiment.subExperiments)
      children = children :+ XMLElement("subExperiment", Map(), "", subExperiment.map(writeValueSet))

    XMLElement("experiment", attributes, "", children)
  }
}
