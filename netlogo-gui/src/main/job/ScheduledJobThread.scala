// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.job

import java.util.{ ArrayList => JArrayList, Comparator, UUID }
import java.util.concurrent.{ BlockingQueue, PriorityBlockingQueue, TimeUnit }

import org.nlogo.api.HaltSignal
import org.nlogo.internalapi.{ JobScheduler => ApiJobScheduler, JobDone, JobErrored,
  JobHalted, ModelOperation, ModelUpdate, MonitorsUpdate, SuspendableJob, TaggedTask }

import scala.annotation.tailrec
import scala.util.{ Failure, Success, Try }
import scala.collection.JavaConverters._
import scala.collection.mutable.SortedSet
import scala.math.Ordering

object ScheduledJobThread {
  sealed trait ScheduledTask extends TaggedTask {
    def submissionTime: Long
    def tag: String
    def stoppable: Boolean
  }

  case class ScheduleOperation(op: ModelOperation, tag: String, submissionTime: Long) extends ScheduledTask {
    val stoppable = true
  }
  case class ScheduleArbitraryAction(op: () => Unit, tag: String, submissionTime: Long) extends ScheduledTask {
    val stoppable = true
  }
  // Q: Why not have AddJob and RunJob be the same?
  // A: I don't think that's a bad idea, per se, but I want to allow flexibility in the future.
  //    Having RunJob events created only by the Scheduler allows them
  //    to hold additional information about the job including things
  //    like "how long since it was run last" and "how long is it estimated to run for"
  //    that isn't available when adding the job. RG 3/28/17
  case class AddJob(job: SuspendableJob, tag: String, interval: Long, submissionTime: Long) extends ScheduledTask {
    val stoppable = true
  }
  case class RunJob(job: SuspendableJob, tag: String, interval: Long, submissionTime: Long) extends ScheduledTask {
    val stoppable = job.intact
  }
  case class StopJob(cancelTag: String, submissionTime: Long)                               extends ScheduledTask {
    val tag = cancelTag + "-stop"
    val stoppable = false
  }
  case class AddMonitor(op: SuspendableJob, tag: String, submissionTime: Long)              extends ScheduledTask {
    val stoppable = false
  }
  case class RunMonitors(monitorOps: Map[String, SuspendableJob], submissionTime: Long)     extends ScheduledTask {
    val tag = "runmonitors"
    val stoppable = false
  }

  object JobSuspension {
    implicit object SuspensionOrdering extends Ordering[JobSuspension] {
      def compare(x: JobSuspension, y: JobSuspension): Int = {
        if (x.timeUnsuspended < y.timeUnsuspended) -1
        else if (x.timeUnsuspended > y.timeUnsuspended) 1
        else 0
      }
    }
  }
  case class JobSuspension(interval: Long, event: ScheduledTask) {
    val timeUnsuspended = event.submissionTime + interval
  }

  object PriorityOrder extends Comparator[ScheduledTask] {
    // lower means "first" or higher priority
    def basePriority(e: ScheduledTask): Int = {
      e match {
        case ScheduleArbitraryAction(_, _, _) => 0
        case ScheduleOperation(_, _, _) => 0
        case StopJob(_, _)              => 1
        case AddJob(_, _, _, _)         => 2
        case AddMonitor(_, _, _)        => 2
        case RunJob(_, _, _, _)         => 3
        case RunMonitors(_, _)          => 3
      }
    }
    def compare(e1: ScheduledTask, e2: ScheduledTask): Int = {
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
  val StepsPerRun = 1000
  val MonitorInterval = 100

  def timeout = 50

  def timeoutUnit: TimeUnit = TimeUnit.MILLISECONDS

  type Task = ScheduledTask

  def handleModelOperation: ModelOperation => Try[ModelUpdate]

  // queue contains *inbound* information about tasks the jobs queue is expected
  // to perform. This variable is written to by any thread and read by the job
  // thread. Note that the job thread itself writes to this frequently.
  def queue: BlockingQueue[ScheduledTask]

  // updates contains *outbound* information about the state of the enqueued jobs
  // this variable is typically written to on the event thread and read elsewhere
  def updates: BlockingQueue[ModelUpdate]

  // suspended jobs is a sorted set of job suspensions. This is expected to only
  // be modified by the job thread (and is therefore private).
  // A scala collection was chosen for convenience reasons, a Java collection
  // might offer better performance.
  private val suspendedJobs = SortedSet.empty[JobSuspension]

  // haltRequested tracks whether the user has requested a halt.
  // It is only read from the job thread, but it written from any thread.
  @volatile var haltRequested: Boolean = false

  // NOTE: These are *not* volatile because it should only ever be accessed on the
  // background thread. Do *NOT* modify these from any other thread.
  //
  // We opted to use scala collections here because we think this will be empty most of the time
  // if we're ever in a situation where this is going to be non-empty or have more than a few
  // elements, consider using java collections instead
  private var stopList        = Set.empty[String]
  private var pendingMonitors = Map.empty[String, SuspendableJob]
  private var monitorsRunning = false
  private var monitorDataStale = true

  def currentTime: Long = System.currentTimeMillis

  private def generateJobTag = UUID.randomUUID.toString

  def createJob(job: SuspendableJob): ScheduledTask = createJob(job, 0)

  def createJob(job: SuspendableJob, interval: Long): ScheduledTask =
    AddJob(job, generateJobTag, interval, currentTime)

  def createOperation(op: ModelOperation): ScheduledTask =
    ScheduleOperation(op, generateJobTag, currentTime)

  def createOperation(op: () => Unit): ScheduledTask =
    ScheduleArbitraryAction(op, generateJobTag, currentTime)

  def queueTask(a: Task): Unit = { queue.add(a) }

  def registerMonitor(name: String, op: SuspendableJob): Unit = {
    queue.add(AddMonitor(op, name, currentTime))
  }

  def stopJob(jobTag: String): Unit = {
    queue.add(StopJob(jobTag, currentTime))
  }

  def stepTask(): Unit = {
    queue.poll(timeout, timeoutUnit) match {
      case null                          => clearStopList()
      case t: ScheduledTask if t.stoppable && stopList.contains(t.tag) => jobCompleted(t.tag)
      case t => processTask(t)
    }
    rescheduleSuspendedJobs()
  }

  def halt(): Unit = {
    haltRequested = true
  }

  def processTask(t: ScheduledTask): Unit = {
    t match {
      case RunJob(job, tag, interval, _) => runJob(job, tag, interval)
      case ScheduleArbitraryAction(op, tag, _) => runArbitraryAction(op, tag)
      case ScheduleOperation(op, tag, _) => runOperation(op)
      case AddJob(job, tag, interval, _) =>
        job.scheduledBy(this)
        queue.add(RunJob(job, tag, interval, currentTime))
      case StopJob(cancelTag, time)      => stopList += cancelTag
      case AddMonitor(op, tag, _)        => addMonitor(tag, op)
      case RunMonitors(updaters, time)   =>
        pendingMonitors.values.foreach(_.scheduledBy(this))
        val allMonitors = updaters ++ pendingMonitors
        pendingMonitors = Map.empty[String, SuspendableJob]
        runMonitors(allMonitors)
    }
  }

  private def rescheduleSuspendedJobs(): Unit = {
    val toSchedule = suspendedJobs.takeWhile(_.timeUnsuspended < currentTime)
    toSchedule.foreach {
      case s@JobSuspension(i, e) =>
        queue.add(e)
        suspendedJobs -= s
    }
  }

  private def clearStopList(): Unit = {
    if (suspendedJobs.isEmpty)
      stopList.foreach(jobCompleted)
    else
      suspendedJobs.foreach {
        case js@JobSuspension(_, RunJob(_, t, _, _)) if stopList.contains(t) =>
          suspendedJobs -= js
          jobCompleted(t)
        case _ =>
      }
  }

  private def jobCompleted(tag: String): Unit = {
    stopList -= tag
    updates.add(JobDone(tag))
  }

  private def jobHalted(tag: String, e: RuntimeException with HaltSignal): Unit = {
    updates.add(JobHalted(tag))
    if (e.haltAll)
      haltAllJobs()
  }

  protected def haltAllJobs(): Unit = {
    def haltTask(s: ScheduledTask): Unit =
      s match {
        case op: ScheduledTask => updates.add(JobHalted(op.tag))
        case _ =>
      }
    suspendedJobs.foreach(s => haltTask(s.event))
    suspendedJobs.clear()
    val queuedTasks = new JArrayList[ScheduledTask]()
    queue.drainTo(queuedTasks)
    queuedTasks.asScala.foreach(haltTask _)
    haltRequested = false
    pendingMonitors = Map.empty[String, SuspendableJob]
  }

  private def runTask[T](task: => T, tag: String, handleSuccess: PartialFunction[Try[T], Unit]): Unit = {
    monitorDataStale = true
    val handleFailures: PartialFunction[Try[T], Unit] = {
      case Failure(e: RuntimeException with HaltSignal) => jobHalted(tag, e)
      case Failure(e: RuntimeException) => updates.add(JobErrored(tag, e))
      case Failure(e)                   => throw e
    }
    (handleSuccess orElse handleFailures).apply(Try(task))
  }

  private def runJob(job: SuspendableJob, tag: String, interval: Long): Unit =
    runTask[Option[SuspendableJob]](job.runFor(StepsPerRun), tag, {
      case Success(None)                => jobCompleted(tag)
      case Success(Some(j))             =>
        val reRun = RunJob(j, tag, interval, currentTime)
        if (interval == 0)  queue.add(reRun)
        else                suspendedJobs += JobSuspension(interval, reRun)
    })

  private def runArbitraryAction(op: () => Unit, tag: String): Unit =
    runTask[Unit](op(), tag, { case Success(()) => jobCompleted(tag) })

  private def runOperation(op: ModelOperation): Unit = {
    monitorDataStale = true
    handleModelOperation(op).foreach(updates.add _)
  }

  private def addMonitor(tag: String, op: SuspendableJob): Unit = {
    if (monitorsRunning) pendingMonitors += tag -> op
    else {
      queue.add(RunMonitors(Map(tag -> op), currentTime))
      monitorsRunning = true
    }
  }

  private def runMonitors(ops: Map[String, SuspendableJob]): Unit = {
    if (monitorDataStale) {
      val (monitorValues, halted) = runMonitorRec(ops, Map.empty[String, Try[AnyRef]])
      updates.add(MonitorsUpdate(monitorValues, currentTime))
      if (! halted) {
        queue.add(RunMonitors(ops, currentTime))
        monitorDataStale = false
      }
    } else {
      queue.add(RunMonitors(ops, currentTime + MonitorInterval))
    }
  }

  @tailrec
  private def runMonitorRec(ops: Map[String, SuspendableJob], acc: Map[String, Try[AnyRef]]): (Map[String, Try[AnyRef]], Boolean) = {
    if (ops.isEmpty) (acc, false)
    else {
      val (key, job) = ops.head
      Try(job.runResult()) match {
        case Failure(e: RuntimeException with HaltSignal) =>
          haltAllJobs()
          (acc, true)
        case other => runMonitorRec(ops.tail, acc + (key -> other))
      }
    }
  }
}

class ScheduledJobThread(val updates: BlockingQueue[ModelUpdate], val handleModelOperation: (ModelOperation => Try[ModelUpdate]))
  extends Thread(null, null, "ScheduledJobThread", JobThread.stackSize * 1024 * 1024)
  with JobScheduler {

  val queue = new PriorityBlockingQueue[ScheduledTask](100, ScheduledJobThread.PriorityOrder)

  @volatile private var dying = false

  setPriority(Thread.NORM_PRIORITY - 1)
  start()

  override def run(): Unit = {
    while (! dying) {
      try {
        stepTask()
      } catch {
        case i: InterruptedException =>
        case e: Exception =>
          println(e)
          e.printStackTrace()
      }
      if (haltRequested) {
        haltAllJobs()
      }
    }
  }

  override def halt(): Unit = {
    if (! haltRequested) {
      super.halt()
      interrupt()
    }
  }

  def die(): Unit = {
    dying = true
    haltRequested = true
    interrupt()
    join()
  }
}
