// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.api.{ Dump, LabProtocol, RefEnumeratedValueSet, RefValueSet, SteppedValueSet, Version }
import org.nlogo.core.{ LiteralParser, VersionUtils, XMLElement }

object LabXMLLoader {

  def readExperiment( element: XMLElement, literalParser: LiteralParser, editNames: Boolean
                    , existingNames: Set[String], modelVersion: Option[String]): (LabProtocol, Set[String]) = {

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

    // these will be loaded into the GUI, however using defaultGUIProtocol causes default values like setup
    // and go to be added in automatically if they aren't specified in the model file (Isaac B 7/11/25)
    val lab = LabProtocol.defaultCodeProtocol(name).copy(
      repetitions = element("repetitions").toInt,
      sequentialRunOrder = element("sequentialRunOrder").toBoolean,
      runMetricsEveryStep = element("runMetricsEveryStep").toBoolean,
      timeLimit = element("timeLimit", "0").toInt
    )

    val stripNewlines: Boolean = modelVersion.orElse(element.get("version")).fold(false) {
      VersionUtils.numericValue(_) >= VersionUtils.numericValue("NetLogo 7.1.0-alpha1")
    }

    element.children.foreach {
      case XMLElement("preExperiment", _, text, _, _) =>
        lab.preExperimentCommands = text
      case XMLElement("setup", _, text, _, _) =>
        if (stripNewlines) {
          lab.setupCommands = text.stripPrefix("\n").stripSuffix("\n")
        } else {
          lab.setupCommands = text
        }
      case XMLElement("go", _, text, _, _) =>
        if (stripNewlines) {
          lab.goCommands = text.stripPrefix("\n").stripSuffix("\n")
        } else {
          lab.goCommands = text
        }
      case XMLElement("postRun", _, text, _, _) =>
        lab.postRunCommands = text
      case XMLElement("postExperiment", _, text, _, _) =>
        lab.postExperimentCommands = text
      case XMLElement("runMetricsCondition", _, text, _, _) =>
        lab.runMetricsCondition = text
      case XMLElement("exitCondition", _, text, _, _) =>
        lab.exitCondition = text
      case el @ XMLElement("metrics", _, _, _, _) =>
        lab.metricsForSaving = el.getChildren("metric").map(_.text).toList
      case XMLElement("constants", _, _, children, _) =>
        lab.constants = children.map(readValueSet).toList
      case el @ XMLElement("subExperiments", _, _, _, _) =>
        lab.subExperiments = el.getChildren("subExperiment").map(_.children.map(readValueSet).toList).toList
      case XMLElement(otherName, _, _, _, _) =>
        throw new Exception(s"Unknown BehaviorSpace XML node type: ${otherName}")
    }

    val outNameMaybe =
      if (editNames)
        Option(
          if (name.nonEmpty)
            if (existingNames.contains(name))
              s"$name (${LazyList.from(1).dropWhile(x => existingNames.contains(s"$name ($x)")).head})"
            else
              name
          else if (existingNames.contains("no name"))
            s"no name (${LazyList.from(1).dropWhile(x => existingNames.contains(s"no name ($x)")).head})"
          else
            "no name"
        )
      else
        None

    outNameMaybe.foreach(lab.name = _)

    (lab, existingNames ++ outNameMaybe)

  }

  def writeExperiment(experiment: LabProtocol, includeVersion: Boolean): XMLElement = {

    def makeBabyMaybe(cond: => Boolean)
                      (tagName: String, text: String, subBabies: Seq[XMLElement],
                       alwaysCDATA: Boolean = false): Option[XMLElement] =
      if (cond)
        Option(XMLElement(tagName, Map(), text, subBabies, alwaysCDATA))
      else
        None

    def makeBabyMaybeSimple(getValue: (LabProtocol) => String, tagName: String): Option[XMLElement] =
      makeBabyMaybe(getValue(experiment).trim.nonEmpty)(tagName, getValue(experiment).trim, Seq())

    def makeBabyMaybeWrapped(getValue: (LabProtocol) => String, tagName: String): Option[XMLElement] = {
      val text: String = getValue(experiment).trim

      if (text.contains("\n")) {
        makeBabyMaybe(getValue(experiment).trim.nonEmpty)(tagName, s"\n$text\n", Seq(), true)
      } else {
        makeBabyMaybe(getValue(experiment).trim.nonEmpty)(tagName, text, Seq())
      }
    }

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

    val baseAttributes = {

      val required =
        Map( "name"                -> experiment.name
           , "repetitions"         -> experiment.repetitions.toString
           , "sequentialRunOrder"  -> experiment.sequentialRunOrder.toString
           , "runMetricsEveryStep" -> experiment.runMetricsEveryStep.toString
           )

      val extras =
        if (includeVersion)
          Map("version" -> Version.version)
        else
          Map()

      required ++ extras

    }

    val attributes =
      baseAttributes ++
        (if (experiment.timeLimit != 0) Map("timeLimit" -> experiment.timeLimit.toString) else Map())

    val subMetrics = experiment.metricsForSaving.flatMap((m) => makeBabyMaybe(true)("metric", m, Seq()))

    val children =
      Seq[XMLElement]() ++
        makeBabyMaybeSimple(_. preExperimentCommands,       "preExperiment") ++
        makeBabyMaybeWrapped(_.        setupCommands,               "setup") ++
        makeBabyMaybeWrapped(_.           goCommands,                  "go") ++
        makeBabyMaybeSimple(_.       postRunCommands,             "postRun") ++
        makeBabyMaybeSimple(_.postExperimentCommands,      "postExperiment") ++
        makeBabyMaybeSimple(_.         exitCondition,       "exitCondition") ++
        makeBabyMaybeSimple(_.   runMetricsCondition, "runMetricsCondition") ++
        makeBabyMaybe(experiment.metricsForSaving.nonEmpty)("metrics", "", subMetrics) ++
        makeBabyMaybe(experiment.constants.nonEmpty)("constants", "", experiment.constants.map(writeValueSet)) ++
        makeBabyMaybe(experiment.subExperiments.nonEmpty)("subExperiments", "",
          experiment.subExperiments.flatMap((se) => makeBabyMaybe(true)("subExperiment", "", se.map(writeValueSet))))

    XMLElement("experiment", attributes, "", children)

  }

}
