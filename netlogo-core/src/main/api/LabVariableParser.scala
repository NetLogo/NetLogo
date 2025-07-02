// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ I18N, LogoList }

import scala.util.Try

// moved variable parsing out of ProtocolEditable so it could be reused for bspace extension
object LabVariableParser {
  // if parsing successful, returns Success((constants, subExperiments))
  def parseVariables(variables: String, repetitions: Int, worldLock: AnyRef, compiler: CompilerServices):
    Try[(List[RefValueSet], List[List[RefValueSet]])] = Try {

    val list = compiler.readFromString("[" + variables + "]").asInstanceOf[LogoList]
    var totalCombinations = 1
    var constants = List[RefValueSet]()
    var subExperiments = List[List[RefValueSet]]()
    for (o <- list.toList) {
      if (!o.isInstanceOf[LogoList])
        throw new Exception(I18N.gui.get("edit.behaviorSpace.expectedList"))

      o.asInstanceOf[LogoList].toList match {
        case List() => throw new Exception(I18N.gui.getN("edit.behaviorSpace.list.field"))
        case List(variableName: String, more: LogoList) =>
          more.toList match {
            case List(first: java.lang.Double,
                      step: java.lang.Double,
                      last: java.lang.Double) =>
              if (step == 0) {
                throw new Exception(I18N.gui.getN("edit.behaviorSpace.list.zeroIncrement", variableName))
              } else if (((last > first && step > 0) || (last < first && step < 0)) &&
                    Int.MaxValue / totalCombinations > ((last - first) / step + 1)) {
                val multiplier: Int = ((last - first) / step + 1).toInt
                totalCombinations = totalCombinations * (if (multiplier == 0) 1 else multiplier)
              } else
                throw new Exception(I18N.gui.getN("edit.behaviorSpace.list.inverseIncrement",
                                    s"[ ${"\"" + variableName + "\""} [ $first $step $last ] ]"))
              val constant = new SteppedValueSet(variableName,
                                                 BigDecimal(Dump.number(first)),
                                                 BigDecimal(Dump.number(step)),
                                                 BigDecimal(Dump.number(last)))
              if (constants.exists(_.variableName == variableName)) {
                throw new Exception(I18N.gui.getN("edit.behaviorSpace.constantDefinedTwice", variableName))
              }
              if (!subExperiments.isEmpty) {
                throw new Exception(I18N.gui.getN("edit.behaviorSpace.constantDefinedSubexperiment", variableName))
              }
              constants = constants :+ constant
            case _ =>
              throw new Exception(I18N.gui.getN("edit.behaviorSpace.list.incrementInvalid", Dump.list(more)))
          }
        case List(variableName: String, more@_*) =>
          if (more.isEmpty) { throw new Exception(I18N.gui.getN("edit.behaviorSpace.expectedValue", variableName)) }
          if (Int.MaxValue / totalCombinations > more.toList.size)
            totalCombinations = totalCombinations * more.toList.size
          else throw new Exception(I18N.gui.getN("edit.behaviorSpace.list.variablelist", variableName))
          val constant = new RefEnumeratedValueSet(variableName, more.toList)
          if (constants.exists(_.variableName == variableName)) {
            throw new Exception(I18N.gui.getN("edit.behaviorSpace.constantDefinedTwice", variableName))
          }
          if (!subExperiments.isEmpty) {
            throw new Exception(I18N.gui.getN("edit.behaviorSpace.constantDefinedSubexperiment", variableName))
          }
          constants = constants :+ constant
        case List(first: LogoList, more@_*) =>
          var subExperiment = List[RefValueSet]()
          (List(first) ++ more).foreach(l => {
            if (!l.isInstanceOf[LogoList])
              throw new Exception(I18N.gui.get("edit.behaviorSpace.expectedList"))

            l.asInstanceOf[LogoList].toList match {
              case List() => throw new Exception(I18N.gui.getN("edit.behaviorSpace.list.field"))
              case List(variableName: String, more: LogoList) =>
                more.toList match {
                  case List(first: java.lang.Double,
                            step: java.lang.Double,
                            last: java.lang.Double) =>
                    if (step == 0) {
                      throw new Exception(I18N.gui.getN("edit.behaviorSpace.list.zeroIncrement", variableName))
                    } else if (((last > first && step > 0) || (last < first && step < 0)) &&
                          Int.MaxValue / totalCombinations > ((last - first) / step + 1)) {
                      val multiplier: Int = ((last - first) / step + 1).toInt
                      totalCombinations = totalCombinations * (if (multiplier == 0) 1 else multiplier)
                    } else
                      throw new Exception(I18N.gui.getN("edit.behaviorSpace.list.inverseIncrement",
                                                  s"[ ${"\"" + variableName + "\""} [ $first $step $last ] ]"))
                    val exp = new SteppedValueSet(variableName,
                                                  BigDecimal(Dump.number(first)),
                                                  BigDecimal(Dump.number(step)),
                                                  BigDecimal(Dump.number(last)))
                    if (subExperiment.exists(_.variableName == variableName)) {
                      throw new Exception(I18N.gui.getN("edit.behaviorSpace.variableDefinedTwiceSubexperiment",
                        variableName))
                    }
                    subExperiment = subExperiment :+ exp
                  case _ =>
                    throw new Exception(I18N.gui.getN("edit.behaviorSpace.list.incrementInvalid", Dump.list(more)))
                }
              case List(variableName: String, more@_*) =>
                if (more.isEmpty) { throw new Exception(I18N.gui.getN("edit.behaviorSpace.expectedValue", variableName)) }
                if (Int.MaxValue / totalCombinations > more.toList.size)
                  totalCombinations = totalCombinations * more.toList.size
                else throw new Exception(I18N.gui.getN("edit.behaviorSpace.list.variablelist", variableName))
                val exp = new RefEnumeratedValueSet(variableName, more.toList)
                if (subExperiment.exists(_.variableName == variableName)) {
                  throw new Exception(I18N.gui.getN("edit.behaviorSpace.variableDefinedTwiceSubexperiment", variableName))
                }
                subExperiment = subExperiment :+ exp
              case _ =>
                throw new Exception(I18N.gui.get("edit.behaviorSpace.invalidFormat"))
            }
          })
          subExperiments = subExperiments :+ subExperiment
        case _ =>
          throw new Exception(I18N.gui.get("edit.behaviorSpace.invalidFormat"))
      }
    }
    for (experiment <- subExperiments) {
      for (valueSet <- experiment) {
        if (!constants.exists(_.variableName == valueSet.variableName) &&
            subExperiments.exists(!_.exists(_.variableName == valueSet.variableName))) {
          throw new Exception(I18N.gui.getN("edit.behaviorSpace.constantDefinedSubexperiment", valueSet.variableName))
        }
      }
    }
    if (repetitions <= 0 || Int.MaxValue / repetitions < totalCombinations)
      throw new Exception(I18N.gui.getN("edit.behaviorSpace.repetition.totalrun"))
    (constants, subExperiments)
  }

  def combineVariables(constants: List[RefValueSet], subExperiments: List[List[RefValueSet]]): String = {
    def setString(valueSet: RefValueSet) =
      "[\"" + valueSet.variableName + "\" " +
      (valueSet match {
         case evs: RefEnumeratedValueSet =>
           evs.map(x => Dump.logoObject(x.asInstanceOf[AnyRef], true, false)).mkString(" ")
         case svs: SteppedValueSet =>
           List(svs.firstValue, svs.step, svs.lastValue).map(_.toString).mkString("[", " ", "]")
       }) + "]"
    (constants.map(setString) :::
     subExperiments.map("[" + _.map(setString).mkString + "]")).mkString("\n")
  }
}
