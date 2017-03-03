// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.agent.Agent
import org.nlogo.core.Femto
import org.nlogo.nvm.{ Job, JobManagerInterface }

trait JobManagement extends AbstractWorkspace {
  val jobManager: JobManagerInterface =
    Femto.get[JobManagerInterface]("org.nlogo.job.JobManager", this, world, world)

  @throws(classOf[InterruptedException])
  abstract override def dispose(): Unit = {
    super.dispose()
    jobManager.die()
  }

  abstract override def halt(): Unit = {
    super.halt()
    jobManager.haltPrimary()
  }

  def joinForeverButtons(agent: Agent): Unit = {
    jobManager.joinForeverButtons(agent)
  }

  def addJobFromJobThread(job: Job): Unit = {
    jobManager.addJobFromJobThread(job)
  }
}
