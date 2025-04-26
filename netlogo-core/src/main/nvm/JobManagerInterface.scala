// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.agent.{Agent, AgentSet}
import org.nlogo.api.{JobOwner, LogoException}

trait JobManagerInterface {
  def isInterrupted: Boolean
  def interrupt(): Unit
  @throws(classOf[InterruptedException])
  def die(): Unit
  def timeToRunSecondaryJobs(): Unit
  def maybeRunSecondaryJobs(): Unit
  def onJobThread: Boolean
  def anyPrimaryJobs(): Boolean
  def addJob(job: Job, waitForCompletion: Boolean): Unit
  def makeConcurrentJob(owner: JobOwner, agentset: AgentSet, workspace: Workspace, procedure: Procedure): Job
  @throws(classOf[LogoException])
  def callReporterProcedure(owner: JobOwner, agentset: AgentSet, workspace: Workspace, procedure: Procedure): AnyRef
  def addReporterJobAndWait(owner: JobOwner, agentset: AgentSet, workspace: Workspace, procedure: Procedure): AnyRef
  def addJobFromJobThread(job: Job): Unit
  def addJob(owner: JobOwner, agents: AgentSet, workspace: Workspace, procedure: Procedure): Unit
  def addSecondaryJob(owner: JobOwner, agents: AgentSet, workspace: Workspace, procedure: Procedure): Unit
  def joinForeverButtons(agent: Agent): Unit
  def haltPrimary(): Unit
  def haltNonObserverJobs(): Unit
  def finishJobs(owner: JobOwner): Unit
  def finishSecondaryJobs(owner: JobOwner): Unit
  def haltSecondary(): Unit
  def stoppingJobs(owner: JobOwner): Unit
}
