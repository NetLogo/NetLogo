// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ LocaleContext, LogoList }

import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.util.Try

// moved variable parsing out of ProtocolEditable so it could be reused for bspace extension
object LabVariableParser {
  // if parsing successful, returns Success((constants, subExperiments))
  def parseVariables(variables: String, repetitions: Int, worldLock: AnyRef,
                     compiler: CompilerServices & GlobalsIdentifier):
    Try[(List[RefValueSet], List[List[RefValueSet]])] = Try {

    val list = compiler.readFromString("[" + variables + "]").asInstanceOf[LogoList]
    var totalCombinations = 1
    var constants = List[RefValueSet]()
    var subExperiments = List[List[RefValueSet]]()
    for (o <- list.toList) {
      if (!o.isInstanceOf[LogoList])
        throw new Exception(loc"Expected list")

      o.asInstanceOf[LogoList].toList match {
        case List() => throw new Exception(loc"""One or more lists do not contain any elements""")
        case List(variableName: String, more: LogoList) =>
          more.toList match {
            case List(first: java.lang.Double,
                      step: java.lang.Double,
                      last: java.lang.Double) =>
              if (step == 0) {
                val message = loc"""|Step value in increment list for variable "$variableName" is 0,
                                    |this would result in an infinite range."""

                throw new Exception(message)
              } else if (((last > first && step > 0) || (last < first && step < 0)) &&
                    Int.MaxValue / totalCombinations > ((last - first) / step + 1)) {
                val multiplier: Int = ((last - first) / step + 1).toInt
                totalCombinations = totalCombinations * (if (multiplier == 0) 1 else multiplier)
              } else {
                val message = loc"""|Increment list "["variableName" [ $first $step $last ] ]" is
                                    |unbounded, reverse sign of step value"""

                throw new Exception(message)
              }
              compiler.checkGlobalVariable(variableName, List(first).asJava)
              val constant = new SteppedValueSet(variableName,
                                                 BigDecimal(Dump.number(first)),
                                                 BigDecimal(Dump.number(step)),
                                                 BigDecimal(Dump.number(last)))
              if (constants.exists(_.variableName == variableName)) {
                throw new Exception(loc"""Constant "$variableName" defined twice""")
              }
              if (!subExperiments.isEmpty) {
                val message = loc"""|The variable "$variableName" does not have an assigned value in at least one
                                    |subexperiment. It must be explicitly defined in all subexperiments or defined
                                    |first as a constant."""

                throw new Exception(message)
              }
              constants = constants :+ constant
            case _ =>
              val message = loc"""|Invalid increment notation for "${Dump.list(more)}", the increment list must have
                                  |exactly 3 number values."""

              throw new Exception(message)
          }
        case List(variableName: String, more@_*) =>
          if (more.isEmpty) { throw new Exception(loc"""Expected a value for variable "$variableName""") }
          if (Int.MaxValue / totalCombinations > more.toList.size)
            totalCombinations = totalCombinations * more.toList.size
          else throw new Exception(loc"""The number of variable combinations for "$variableName" exceeds limit""")
          compiler.checkGlobalVariable(variableName, more.toList.asJava)
          val constant = new RefEnumeratedValueSet(variableName, more.toList)
          if (constants.exists(_.variableName == variableName)) {
            throw new Exception(loc"""Constant "$variableName" defined twice""")
          }
          if (!subExperiments.isEmpty) {
            val message = loc"""|The variable "$variableName" does not have an assigned value in at least one
                                |subexperiment. It must be explicitly defined in all subexperiments or defined
                                |first as a constant."""

            throw new Exception(message)
          }
          constants = constants :+ constant
        case List(first: LogoList, more@_*) =>
          var subExperiment = List[RefValueSet]()
          (List(first) ++ more).foreach(l => {
            if (!l.isInstanceOf[LogoList])
              throw new Exception(loc"Expected list")

            l.asInstanceOf[LogoList].toList match {
              case List() => throw new Exception(loc"""One or more lists do not contain any elements""")
              case List(variableName: String, more: LogoList) =>
                more.toList match {
                  case List(first: java.lang.Double,
                            step: java.lang.Double,
                            last: java.lang.Double) =>
                    if (step == 0) {
                      val message = loc"""|Step value in increment list for variable "$variableName" is 0,
                                          |this would result in an infinite range."""

                      throw new Exception(message)
                    } else if (((last > first && step > 0) || (last < first && step < 0)) &&
                          Int.MaxValue / totalCombinations > ((last - first) / step + 1)) {
                      val multiplier: Int = ((last - first) / step + 1).toInt
                      totalCombinations = totalCombinations * (if (multiplier == 0) 1 else multiplier)
                    } else {
                      val message = loc"""|Increment list "[ ${"\"" + variableName + "\""} [ $first $step $last ] ]" is
                                    |unbounded, reverse sign of step value"""

                      throw new Exception(message)
                    }
                    compiler.checkGlobalVariable(variableName, more.toList.asJava)
                    val exp = new SteppedValueSet(variableName,
                                                  BigDecimal(Dump.number(first)),
                                                  BigDecimal(Dump.number(step)),
                                                  BigDecimal(Dump.number(last)))
                    if (subExperiment.exists(_.variableName == variableName))
                      throw new Exception(loc"""Variable "$variableName" defined twice in one subexperiment""")
                    subExperiment = subExperiment :+ exp
                  case _ =>
                    val message = loc"""|Invalid increment notation for "${Dump.list(more)}", the increment list must
                                        |have exactly 3 number values."""

                    throw new Exception(message)
                }
              case List(variableName: String, more@_*) =>
                if (more.isEmpty) { throw new Exception(loc"""Expected a value for variable "$variableName""") }
                if (Int.MaxValue / totalCombinations > more.toList.size)
                  totalCombinations = totalCombinations * more.toList.size
                else throw new Exception(loc"""The number of variable combinations for "$variableName" exceeds limit""")
                compiler.checkGlobalVariable(variableName, more.toList.asJava)
                val exp = new RefEnumeratedValueSet(variableName, more.toList)
                if (subExperiment.exists(_.variableName == variableName))
                  throw new Exception(loc"""Variable "$variableName" defined twice in one subexperiment""")
                subExperiment = subExperiment :+ exp
              case _ =>
                throw new Exception(loc"Invalid format")
            }
          })
          subExperiments = subExperiments :+ subExperiment
        case _ =>
          throw new Exception(loc"Invalid format")
      }
    }
    for (experiment <- subExperiments) {
      for (valueSet <- experiment) {
        if (!constants.exists(_.variableName == valueSet.variableName) &&
            subExperiments.exists(!_.exists(_.variableName == valueSet.variableName))) {
          val message = loc"""|The variable "${valueSet.variableName}" does not have an assigned value in at least one
                              |subexperiment. It must be explicitly defined in all subexperiments or defined
                              |first as a constant."""

          throw new Exception(message)
        }
      }
    }
    if (repetitions <= 0 || Int.MaxValue / repetitions < totalCombinations)
      throw new Exception(loc"Total number of runs exceeds limit")
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
