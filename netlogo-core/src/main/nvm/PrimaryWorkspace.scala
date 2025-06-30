// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.agent.OutputObject

// indicates a workspace that was not created by BehaviorSpace (Isaac B 6/29/25)
trait PrimaryWorkspace {
  private val experimentManager = new ExperimentManager

  def getExperimentManager: ExperimentManager =
    experimentManager

  def mirrorOutput(oo: OutputObject, toOutputArea: Boolean): Unit = {}
  def runtimeError(t: Throwable): Unit = {}
}

class DummyPrimaryWorkspace extends PrimaryWorkspace
