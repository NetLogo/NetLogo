// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.internalapi

trait SuspendableJob {
  def runFor(steps: Int): Option[SuspendableJob]
}

trait JobScheduler {
  def scheduleJob(job: SuspendableJob): String
  def stopJob(jobTag: String): Unit
  def scheduleOperation(op: () => Unit): String
  def die()
}

trait SchedulerWorkspace {
  def scheduledJobThread: JobScheduler
}
