// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.agent.{Agent, AgentSet}
import org.nlogo.api.{JobOwner, LogoException}

trait JobManagerInterface {
  def isInterrupted: Boolean
  def interrupt()
  @throws(classOf[InterruptedException])
  def die()
  def timeToRunSecondaryJobs()
  def maybeRunSecondaryJobs()
  def anyPrimaryJobs(): Boolean
  def addJob(job: Job, waitForCompletion: Boolean)
  def makeConcurrentJob(owner: JobOwner, agentset: AgentSet, workspace: Workspace, procedure: Procedure): Job
  @throws(classOf[LogoException])
  def callReporterProcedure(owner: JobOwner, agentset: AgentSet, workspace: Workspace, procedure: Procedure): AnyRef
  def addReporterJobAndWait(owner: JobOwner, agentset: AgentSet, workspace: Workspace, procedure: Procedure): AnyRef
  def addJobFromJobThread(job: Job)
  def addJob(owner: JobOwner, agents: AgentSet, workspace: Workspace, procedure: Procedure)
  def addSecondaryJob(owner: JobOwner, agents: AgentSet, workspace: Workspace, procedure: Procedure)
  def joinForeverButtons(agent: Agent)
  def haltPrimary()
  def haltNonObserverJobs()
  def finishJobs(owner: JobOwner)
  def finishSecondaryJobs(owner: JobOwner)
  def haltSecondary()
  def stoppingJobs(owner: JobOwner)
}
