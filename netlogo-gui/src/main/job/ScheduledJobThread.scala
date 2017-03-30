// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.job

import java.util.{ Comparator, UUID }
import java.util.concurrent.{ BlockingQueue, PriorityBlockingQueue, TimeUnit }

import org.nlogo.internalapi.{ AddProcedureRun, JobScheduler => ApiJobScheduler,
  JobDone, JobErrored, ModelAction, ModelUpdate, MonitorsUpdate, StopProcedure,
  SuspendableJob, UpdateInterfaceGlobal }

import scala.util.{ Failure, Success, Try }

object ScheduledJobThread {
  sealed trait ScheduledEvent {
    def submissionTime: Long
  }
  case class ScheduleOperation(op: () => Unit, tag: String, submissionTime: Long) extends ScheduledEvent
  case class StopJob(cancelTag: String, submissionTime: Long) extends ScheduledEvent
  case class AddJob(job: SuspendableJob, tag: String, submissionTime: Long) extends ScheduledEvent
  case class AddMonitor(tag: String, op: () => AnyRef, submissionTime: Long) extends ScheduledEvent
  // Q: Why not have AddJob and RunJob be the same?
  // A: I don't think that's a bad idea, per se, but I want to allow flexibility in the future.
  //    Having RunJob events created only the RunJob event to hold additional information
  //    about the job including things like "how long since it was run last" and "how long
  //    is it estimated to run for" that isn't available when adding the job. RG 3/28/17
  case class RunJob(job: SuspendableJob, tag: String, submissionTime: Long) extends ScheduledEvent
  case class RunMonitors(monitorOps: Map[String, () => AnyRef], submissionTime: Long) extends ScheduledEvent

  object PriorityOrder extends Comparator[ScheduledEvent] {
    // lower means "first" or higher priority
    def basePriority(e: ScheduledEvent): Int = {
      e match {
        case ScheduleOperation(_, _, _) => 0
        case StopJob(_, _)              => 1
        case AddJob(_, _, _)            => 2
        case AddMonitor(_, _, _)        => 2
        case RunJob(_, _, _)            => 3
        case RunMonitors(_, _)          => 3
      }
    }
    def compare(e1: ScheduledEvent, e2: ScheduledEvent): Int = {
      val p1 = basePriority(e1)
      val p2 = basePriority(e2)
      if (p1 < p2) -1
      else if (p1 > p2) 1
      else if (e1.submissionTime < e2.submissionTime) -1
      else if (e1.submissionTime > e2.submissionTime) 1
      else 0
    }
  }
}

import ScheduledJobThread._

trait JobScheduler extends ApiJobScheduler {
  val StepsPerRun = 100

  def timeout = 500
  def timeoutUnit: TimeUnit = TimeUnit.MILLISECONDS

  def queue: BlockingQueue[ScheduledEvent]

  def updates: BlockingQueue[ModelUpdate]

  def scheduleJob(job: SuspendableJob): String = {
    val jobTag = UUID.randomUUID.toString
    val e = AddJob(job, jobTag, System.currentTimeMillis)
    queue.add(e)
    jobTag
  }

  def scheduleOperation(op: () => Unit): String = {
    val jobTag = UUID.randomUUID.toString
    val e = ScheduleOperation(op, jobTag, System.currentTimeMillis)
    queue.add(e)
    jobTag
  }

  def registerMonitorUpdate(name: String, op: () => AnyRef): Unit = {
    queue.add(AddMonitor(name, op, System.currentTimeMillis))
  }

  def stopJob(jobTag: String): Unit = {
    queue.add(StopJob(jobTag, System.currentTimeMillis))
  }

  // NOTE: These are *not* volatile because it should only ever be accessed on the
  // background thread. Do *NOT* modify these from any other thread.
  //
  // We opted to use scala collections here because we think this will be empty most of the time
  // if we're ever in a situation where this is going to be non-empty or have more than a few
  // elements, consider using java collections instead
  private var stopList        = Set.empty[String]
  private var pendingMonitors = Map.empty[String, () => AnyRef]
  private var monitorsRunning = false

  def clearStopList(): Unit = {
    stopList.foreach(jobStopped)
  }

  private def jobStopped(tag: String): Unit = {
    stopList -= tag
    updates.add(JobDone(tag))
  }

  // TODO: This doesn't yet return information about completed jobs
  def runEvent(): Unit = {
    queue.poll(timeout, timeoutUnit) match {
      case null                   => clearStopList()
      case AddJob(job, tag, time) =>
        if (stopList.contains(tag)) jobStopped(tag)
        else                        queue.add(RunJob(job, tag, System.currentTimeMillis))
      case RunJob(job, tag, time) =>
        if (stopList.contains(tag)) jobStopped(tag)
        else {
          Try(job.runFor(StepsPerRun)) match {
            case Success(None)    => updates.add(JobDone(tag))
            case Success(Some(j)) => queue.add(RunJob(j, tag, System.currentTimeMillis))
            case Failure(e: RuntimeException) => updates.add(JobErrored(tag, e))
            case Failure(e) => throw e
          }
        }
      case StopJob(cancelTag, time) =>
        stopList += cancelTag
      case ScheduleOperation(op, tag, time) =>
        Try(op()) match {
          case Success(())                  => updates.add(JobDone(tag))
          case Failure(e: RuntimeException) => updates.add(JobErrored(tag, e))
          case Failure(e)                   => throw e
        }
      case AddMonitor(tag, op, time) =>
        if (monitorsRunning) pendingMonitors += tag -> op
        else {
          queue.add(RunMonitors(Map(tag -> op), System.currentTimeMillis))
          monitorsRunning = true
        }
      case RunMonitors(updaters, time) =>
        val allMonitors = updaters ++ pendingMonitors
        pendingMonitors = Map.empty[String, () => AnyRef]
        val monitorValues = allMonitors.map { case (k, op) => k -> Try(op()) }.toMap
        queue.add(RunMonitors(allMonitors, System.currentTimeMillis))
        updates.add(MonitorsUpdate(monitorValues))
    }
  }
}

class ScheduledJobThread(val updates: BlockingQueue[ModelUpdate])
  extends Thread(null, null, "ScheduledJobThread", JobThread.stackSize * 1024 * 1024)
  with JobScheduler {

  val queue = new PriorityBlockingQueue[ScheduledEvent](100, ScheduledJobThread.PriorityOrder)

  @volatile private var dying = false

  setPriority(Thread.NORM_PRIORITY - 1)
  start()

  // TODO: need to implement halt here...
  override def run(): Unit = {
    while (! dying) {
      try {
        runEvent()
      } catch {
        case i: InterruptedException =>
        case e: Exception =>
          println(e)
          e.printStackTrace()
      }
    }
  }

  def die(): Unit = {
    dying = true
    interrupt()
    join()
  }
}
