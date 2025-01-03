// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ CompilerException, I18N, LogoList }

// moved variable parsing out of ProtocolEditable so it could be reused for bspace extension
object LabVariableParser
{
  // if parsing successful, returns (constants, subExperiments) for first value, empty string for second
  // if parsing not successful, returns None for first value, error message for second
  def parseVariables(variables: String, repetitions: Int, worldLock: AnyRef, compiler: CompilerServices): (Option[(List[RefValueSet],
                                                                                         List[List[RefValueSet]])],
                                                                                         String) = {
    val list =
      try { worldLock.synchronized {
        compiler.readFromString("[" + variables + "]").asInstanceOf[LogoList]
      } }
    catch{ case ex: CompilerException => return (None, ex.getMessage) }
    var totalCombinations = 1
    var constants = List[RefValueSet]()
    var subExperiments = List[List[RefValueSet]]()
    for (o <- list.toList) {
      if (!o.isInstanceOf[LogoList])
        return (None, I18N.gui.get("edit.behaviorSpace.expectedList"))

      o.asInstanceOf[LogoList].toList match {
        case List() => return (None, I18N.gui.getN("edit.behaviorSpace.list.field"))
        case List(variableName: String, more: LogoList) =>
          more.toList match {
            case List(first: java.lang.Double,
                      step: java.lang.Double,
                      last: java.lang.Double) =>
              if (((last > first && step > 0) || (last < first && step < 0)) &&
                    Int.MaxValue / totalCombinations > ((last - first) / step + 1)) {
                val multiplier: Int = ((last - first) / step + 1).toInt
                totalCombinations = totalCombinations * (if (multiplier == 0) 1 else multiplier)
              } else
                return (None, I18N.gui.getN("edit.behaviorSpace.list.increment",
                                            s"[ ${'"' + variableName + '"'} [ $first $step $last ] ]"))
              val constant = new SteppedValueSet(variableName,
                                                 BigDecimal(Dump.number(first)),
                                                 BigDecimal(Dump.number(step)),
                                                 BigDecimal(Dump.number(last)))
              if (constants.exists(_.variableName == variableName)) {
                return (None, I18N.gui.getN("edit.behaviorSpace.constantDefinedTwice", variableName))
              }
              if (!subExperiments.isEmpty) {
                return (None, I18N.gui.getN("edit.behaviorSpace.constantDefinedSubexperiment", variableName))
              }
              constants = constants :+ constant
            case _ =>
              return (None, I18N.gui.getN("edit.behaviorSpace.list.incrementInvalid", Dump.list(more)))
          }
        case List(variableName: String, more@_*) =>
          if (more.isEmpty) { return (None, I18N.gui.getN("edit.behaviorSpace.expectedValue", variableName)) }
          if (Int.MaxValue / totalCombinations > more.toList.size)
            totalCombinations = totalCombinations * more.toList.size
          else return (None, I18N.gui.getN("edit.behaviorSpace.list.variablelist", variableName))
          val constant = new RefEnumeratedValueSet(variableName, more.toList)
          if (constants.exists(_.variableName == variableName)) {
            return (None, I18N.gui.getN("edit.behaviorSpace.constantDefinedTwice", variableName))
          }
          if (!subExperiments.isEmpty) {
            return (None, I18N.gui.getN("edit.behaviorSpace.constantDefinedSubexperiment", variableName))
          }
          constants = constants :+ constant
        case List(first: LogoList, more@_*) =>
          var subExperiment = List[RefValueSet]()
          (List(first) ++ more).foreach(l => {
            if (!l.isInstanceOf[LogoList])
              return (None, I18N.gui.get("edit.behaviorSpace.expectedList"))

            l.asInstanceOf[LogoList].toList match {
              case List() => return (None, I18N.gui.getN("edit.behaviorSpace.list.field"))
              case List(variableName: String, more: LogoList) =>
                more.toList match {
                  case List(first: java.lang.Double,
                            step: java.lang.Double,
                            last: java.lang.Double) =>
                    if (((last > first && step > 0) || (last < first && step < 0)) &&
                          Int.MaxValue / totalCombinations > ((last - first) / step + 1)) {
                      val multiplier: Int = ((last - first) / step + 1).toInt
                      totalCombinations = totalCombinations * (if (multiplier == 0) 1 else multiplier)
                    } else
                      return (None, I18N.gui.getN("edit.behaviorSpace.list.increment",
                                                  s"[ ${'"' + variableName + '"'} [ $first $step $last ] ]"))
                    val exp = new SteppedValueSet(variableName,
                                                  BigDecimal(Dump.number(first)),
                                                  BigDecimal(Dump.number(step)),
                                                  BigDecimal(Dump.number(last)))
                    if (subExperiment.exists(_.variableName == variableName)) {
                      return (None, I18N.gui.getN("edit.behaviorSpace.variableDefinedTwiceSubexperiment",
                        variableName))
                    }
                    subExperiment = subExperiment :+ exp
                  case _ =>
                    return (None, I18N.gui.getN("edit.behaviorSpace.list.incrementinvalid", Dump.list(more)))
                }
              case List(variableName: String, more@_*) =>
                if (more.isEmpty) { return (None, I18N.gui.getN("edit.behaviorSpace.expectedValue", variableName)) }
                if (Int.MaxValue / totalCombinations > more.toList.size)
                  totalCombinations = totalCombinations * more.toList.size
                else return (None, I18N.gui.getN("edit.behaviorSpace.list.variablelist", variableName))
                val exp = new RefEnumeratedValueSet(variableName, more.toList)
                if (subExperiment.exists(_.variableName == variableName)) {
                  return (None, I18N.gui.getN("edit.behaviorSpace.variableDefinedTwiceSubexperiment", variableName))
                }
                subExperiment = subExperiment :+ exp
              case _ =>
                return (None, I18N.gui.get("edit.behaviorSpace.invalidFormat"))
            }
          })
          subExperiments = subExperiments :+ subExperiment
        case _ =>
          return (None, I18N.gui.get("edit.behaviorSpace.invalidFormat"))
      }
    }
    for (experiment <- subExperiments) {
      for (valueSet <- experiment) {
        if (!constants.exists(_.variableName == valueSet.variableName) &&
            subExperiments.exists(!_.exists(_.variableName == valueSet.variableName))) {
          return (None, I18N.gui.getN("edit.behaviorSpace.constantDefinedSubexperiment", valueSet.variableName))
        }
      }
    }
    if (repetitions <= 0 || Int.MaxValue / repetitions < totalCombinations)
      return (None, I18N.gui.getN("edit.behaviorSpace.repetition.totalrun"))
    return (Some((constants, subExperiments)), "")
  }

  def combineVariables(constants: List[RefValueSet], subExperiments: List[List[RefValueSet]]): String = {
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
    (constants.map(setString) :::
     subExperiments.map("[" + _.map(setString).mkString + "]")).mkString("\n")
  }
}
