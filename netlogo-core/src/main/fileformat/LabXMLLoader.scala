// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.api.{ Dump, LabProtocol, RefEnumeratedValueSet, RefValueSet, SteppedValueSet, XMLElement }
import org.nlogo.core.LiteralParser

object LabXMLLoader {

  def readExperiment( element: XMLElement, literalParser: LiteralParser, editNames: Boolean
                    , existingNames: Set[String]): (LabProtocol, Set[String]) = {

    def readValueSet(element: XMLElement): RefValueSet = {
      element.name match {
        case "steppedValueSet" =>
          SteppedValueSet( element("variable"), BigDecimal(element("first")), BigDecimal(element("step"))
                         , BigDecimal(element("last")))

        case "enumeratedValueSet" =>
          val value = element.getChildren("value").map(element => literalParser.readFromString(element("value")))
          RefEnumeratedValueSet(element("variable"), value.toList)

      }
    }

    val name = element("name")

    val baseLab =
      LabProtocol( name, "", "", "", "", "", element("repetitions").toInt
                 , element("sequentialRunOrder").toBoolean, element("runMetricsEveryStep").toBoolean, ""
                 , element("timeLimit", "0").toInt, "", List(), List(), List())

    val filledLab =
      element.children.foldLeft(baseLab) {
        case (lab, XMLElement("preExperiment", _, text, _)) =>
          lab.copy(preExperimentCommands = text)
        case (lab, XMLElement("setup", _, text, _)) =>
          lab.copy(setupCommands = text)
        case (lab, XMLElement("go", _, text, _)) =>
          lab.copy(goCommands = text)
        case (lab, XMLElement("postRun", _, text, _)) =>
          lab.copy(postRunCommands = text)
        case (lab, XMLElement("postExperiment", _, text, _)) =>
          lab.copy(postExperimentCommands = text)
        case (lab, XMLElement("runMetricsCondition", _, text, _)) =>
          lab.copy(runMetricsCondition = text)
        case (lab, XMLElement("exitCondition", _, text, _)) =>
          lab.copy(exitCondition = text)
        case (lab, el @ XMLElement("metrics", _, _, _)) =>
          lab.copy(metrics = el.getChildren("metric").map(_.text).toList)
        case (lab, XMLElement("constants", _, _, children)) =>
          lab.copy(constants = children.map(readValueSet).toList)
        case (lab, el @ XMLElement("subExperiments", _, _, _)) =>
          lab.copy(subExperiments = el.getChildren("subExperiment").map(_.children.map(readValueSet).toList).toList)
        case (  _, XMLElement(otherName, _, _, _)) =>
          throw new Exception(s"Unknown BehaviorSpace XML node type: ${otherName}")
      }

    val outNameMaybe =
      if (editNames)
        Option(
          if (name.nonEmpty)
            if (existingNames.contains(name))
              s"$name (${Stream.from(1).dropWhile(x => existingNames.contains(s"$name ($x)")).head})"
            else
              name
          else if (existingNames.contains("no name"))
            s"no name (${Stream.from(1).dropWhile(x => existingNames.contains(s"no name ($x)")).head})"
          else
            "no name"
        )
      else
        None

    (filledLab, existingNames ++ outNameMaybe)

  }

  def writeExperiment(experiment: LabProtocol): XMLElement = {

    def makeBabyMaybe(cond: => Boolean)
                      (tagName: String, text: String, subBabies: Seq[XMLElement]): Option[XMLElement] =
      if (cond)
        Option(XMLElement(tagName, Map(), text, subBabies))
      else
        None

    def makeBabyMaybeSimple(getValue: (LabProtocol) => String, tagName: String): Option[XMLElement] =
      makeBabyMaybe(getValue(experiment).trim.nonEmpty)(tagName, getValue(experiment).trim, Seq())

    def writeValueSet(valueSet: RefValueSet): XMLElement = {
      valueSet match {

        case stepped: SteppedValueSet =>

          val attributes =
            Map( "variable" -> stepped.variableName
               , "first"    -> Dump.number(stepped.firstValue.toDouble)
               , "step"     -> Dump.number(stepped.step.toDouble)
               , "last"     -> Dump.number(stepped.lastValue.toDouble)
               )

          XMLElement("steppedValueSet", attributes, "", Seq())

        case enumerated: RefEnumeratedValueSet =>

          val attributes = Map("variable" -> enumerated.variableName)

          val children =
            for (value <- enumerated.toList)
              yield XMLElement("value", Map("value" -> Dump.logoObject(value, true, false)), "", Seq())

          XMLElement("enumeratedValueSet", attributes, "", children)

      }
    }

    val baseAttributes =
      Map( "name"                -> experiment.name
         , "repetitions"         -> experiment.repetitions.toString
         , "sequentialRunOrder"  -> experiment.sequentialRunOrder.toString
         , "runMetricsEveryStep" -> experiment.runMetricsEveryStep.toString
         )

    val attributes =
      baseAttributes ++
        (if (experiment.timeLimit != 0) Map("timeLimit" -> experiment.timeLimit.toString) else Map())

    val subMetrics = experiment.metrics.flatMap((m) => makeBabyMaybe(true)("metric", m, Seq()))

    val children =
      Seq[XMLElement]() ++
        makeBabyMaybeSimple(_. preExperimentCommands,       "preExperiment") ++
        makeBabyMaybeSimple(_.         setupCommands,               "setup") ++
        makeBabyMaybeSimple(_.            goCommands,                  "go") ++
        makeBabyMaybeSimple(_.       postRunCommands,             "postRun") ++
        makeBabyMaybeSimple(_.postExperimentCommands,      "postExperiment") ++
        makeBabyMaybeSimple(_.         exitCondition,       "exitCondition") ++
        makeBabyMaybeSimple(_.   runMetricsCondition, "runMetricsCondition") ++
        makeBabyMaybe(experiment.metrics.nonEmpty)("metrics", "", subMetrics) ++
        makeBabyMaybe(experiment.constants.nonEmpty)("constants", "", experiment.constants.map(writeValueSet)) ++
        makeBabyMaybe(experiment.subExperiments.nonEmpty)("subExperiments", "",
          experiment.subExperiments.flatMap((se) => makeBabyMaybe(true)("subExperiment", "", se.map(writeValueSet))))

    XMLElement("experiment", attributes, "", children)

  }

}
