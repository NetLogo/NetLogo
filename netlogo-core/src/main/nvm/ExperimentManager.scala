// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.LabProtocol

sealed abstract trait ExperimentType

object ExperimentType {
  case object GUI extends ExperimentType
  case object Code extends ExperimentType
}

case class Experiment(protocol: LabProtocol, tpe: ExperimentType)

class ExperimentManager {
  private var experiments = Map[String, Experiment]()
  private var experimentStack = Map[String, Option[LabInterface.Worker]]()
  private var currentExperiment = ""

  def setGUIExperiments(protocols: Seq[LabProtocol]): Unit = {
    experiments = experiments.filter(_._2.tpe != ExperimentType.GUI) ++ protocols.map { protocol =>
      (protocol.name, Experiment(protocol, ExperimentType.GUI))
    }
  }

  def addExperiment(experiment: Experiment): Unit = {
    experiments = experiments.updated(experiment.protocol.name, experiment)
  }

  def addExperimentToStack(name: String): Boolean = {
    if (experimentStack.contains(name)) {
      false
    } else {
      experimentStack += ((name, None))

      true
    }
  }

  def setStackWorker(name: String, worker: LabInterface.Worker): Unit = {
    experimentStack = experimentStack.updated(name, Option(worker))
  }

  def getExperiment(name: String): Option[Experiment] =
    experiments.get(name)

  def getCurrentExperiment: Option[Experiment] =
    experiments.get(currentExperiment)

  def setCurrentExperiment(name: String): Unit = {
    currentExperiment = name
  }

  def allExperiments: Seq[Experiment] =
    experiments.values.toSeq

  def removeExperiment(name: String): Unit = {
    experiments -= name
  }

  def removeExperimentFromStack(name: String): Unit = {
    experimentStack -= name
  }

  def clearExperiments(): Unit = {
    for ((_, worker) <- experimentStack)
      worker.foreach(_.abort())

    experiments = Map()
    experimentStack = Map()

    currentExperiment = ""
  }
}
