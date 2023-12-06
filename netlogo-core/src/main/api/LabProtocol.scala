// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{ CompilerException, I18N, LogoList }

object LabProtocol {
  def parseVariables(variables: String, worldLock: AnyRef, compiler: CompilerServices, complain: (String) => Unit = (_) => {}): Option[(List[RefValueSet], List[List[RefValueSet]])] = {
    val list =
      try { worldLock.synchronized {
        compiler.readFromString("[" + variables + "]").asInstanceOf[LogoList]
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
    return Some((constants, subExperiments))
  }
}

case class LabProtocol(name: String,
                    preExperimentCommands: String,
                    setupCommands: String,
                    goCommands: String,
                    postRunCommands: String,
                    postExperimentCommands: String,
                    repetitions: Int,
                    sequentialRunOrder: Boolean,
                    runMetricsEveryStep: Boolean,
                    runMetricsCondition: String,
                    timeLimit: Int,
                    exitCondition: String,
                    metrics: List[String],
                    constants: List[RefValueSet],
                    subExperiments: List[List[RefValueSet]] = Nil,
                    runsCompleted: Int = 0,
                    runOptions: LabRunOptions = null)
{
  val valueSets =
    if (subExperiments.isEmpty)
      List(constants)
    else {
      val variables = (constants.map(_.variableName) ::: subExperiments.flatten.map(_.variableName)).distinct
      for (subExperiment <- subExperiments) yield {
        var filled = List[RefValueSet]()
        for (variable <- variables) {
          filled = filled :+ subExperiment.find(_.variableName == variable)
                                          .getOrElse(constants.find(_.variableName == variable)
                                          .getOrElse(new RefEnumeratedValueSet(variable, List(null).asInstanceOf[List[AnyRef]])))
        }
        filled
      }
    }

  def countRuns = repetitions * valueSets.map(_.map(_.length.toInt).product).sum

  // Generate all the possible combinations of values from the ValueSets, in order.  (I'm using
  // Iterator here so that each combination we generate can be garbage collected when we're done
  // with it, instead of them all being held in memory until the end of the experiment.
  // - ST 5/1/08, see bug #63 - ST 2/28/12
  @deprecated("6.0.2", "use AnyRefSettingsIterator instead")
  type SettingsIterator = Iterator[List[(String, Any)]]

  @deprecated("6.0.2", "use refElements instead")
  def elements: SettingsIterator = refElements

  type AnyRefSettingsIterator = Iterator[List[(String, AnyRef)]]

  def refElements: AnyRefSettingsIterator = {
    def combinations(sets: List[RefValueSet]): AnyRefSettingsIterator =
      sets match {
        case Nil => Iterator(Nil)
        case set::sets =>
          set.iterator.flatMap(v =>
            combinations(sets).map(m =>
              if (sequentialRunOrder) (set.variableName,v) :: m
              else m :+ set.variableName -> v))
      }
    if (sequentialRunOrder) {
      valueSets.map(combinations(_).flatMap(x => Iterator.fill(repetitions)(x))).flatten.toIterator
    }
    else {
      Iterator.fill(repetitions)(valueSets.map(x => combinations(x.reverse)).flatten).flatten
    }
  }
}
