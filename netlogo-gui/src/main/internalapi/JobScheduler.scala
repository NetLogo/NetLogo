// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.internalapi

trait SuspendableJob {
  def runFor(steps: Int): Option[SuspendableJob]
  def runResult(): AnyRef
  def scheduledBy(s: JobScheduler): Unit
}

trait JobScheduler {
  def registerMonitor(name: String, job: SuspendableJob): Unit
  def scheduleJob(job: SuspendableJob): String
  def scheduleJob(job: SuspendableJob, interval: Long): String
  def stopJob(jobTag: String): Unit
  def scheduleOperation(op: () => Unit): String
  def haltRequested: Boolean
  def halt(): Unit
  def die(): Unit
}

trait SchedulerWorkspace {
  def scheduledJobThread: JobScheduler
  def setFrameSkips(i: Int): Unit
}
