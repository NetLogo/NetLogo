// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.internalapi

trait SuspendableJob {
  def runFor(steps: Int): Option[SuspendableJob]
  def runResult(): AnyRef
  /* If a suspendable job is "intact", the model remains in a well-formed state
   * whether or not that job is run. If the model does not run a job which
   * is not "intact", it has omitted part of a procedure which should have
   * run to completion and so the model may be in an inconsistent or incomplete state.
   */
  def intact: Boolean
  def scheduledBy(s: JobScheduler): Unit
}

trait TaggedTask {
  def tag: String
}

trait JobScheduler {
  type Task <: TaggedTask

  def createJob(job: SuspendableJob, interval: Long): Task
  def createJob(job: SuspendableJob): Task
  def createOperation(op: ModelOperation): Task
  def createOperation(op: () => Unit): Task
  def stopJob(jobTag: String): Unit
  def registerMonitor(name: String, job: SuspendableJob): Unit


  def queueTask(a: Task): Unit

  def haltRequested: Boolean
  def halt(): Unit
  def die(): Unit
  def clearJobsAndMonitors(): Unit
}

trait SchedulerWorkspace {
  def scheduledJobThread: JobScheduler
  def setMaxFPS(i: Int): Unit
}
