// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.job

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.LinkedBlockingQueue
import java.util.{ Collections, ArrayList }

import org.nlogo.core.AgentKind
import org.nlogo.internalapi.{ JobDone, JobErrored, JobHalted,
  JobScheduler => ApiJobScheduler, ModelUpdate, ModelOperation,
  MonitorsUpdate, SuspendableJob, TicksStarted, UpdateVariable, UpdateSuccess }
import org.nlogo.nvm.HaltException

import org.scalatest.{ FunSuite, Inside }

import scala.collection.JavaConverters._
import scala.util.Try

import ScheduledJobThread._

object ScheduledJobThreadTest {
  class SimpleScheduler extends JobScheduler {
    override def timeout = 10
    val queue = new LinkedBlockingQueue[ScheduledTask]
    val updates = new LinkedBlockingQueue[ModelUpdate]
    def die(): Unit = {}
    var setTime: Long = 0
    override def currentTime = setTime
    var processedOperations = Seq.empty[ModelOperation]
    def handleModelOperation = { (op) =>
      processedOperations :+= op
      op match {
        case uv: UpdateVariable => Try(UpdateSuccess(uv))
      }
    }
  }
}

import ScheduledJobThreadTest._

class ScheduledJobThreadTest extends FunSuite {

  def sortTasks(es: ScheduledTask*): Seq[ScheduledTask] = {
    val a = new ArrayList[ScheduledTask](es.length)
    es.foreach(a.add)
    Collections.sort(a, ScheduledJobThread.PriorityOrder)
    a.asScala
  }
  val dummyJob = new DummyJob()

  def assertSortedOrder(e1: ScheduledTask, e2: ScheduledTask): Unit = {
    assert(sortTasks(e1, e2) == Seq(e1, e2))
    assert(sortTasks(e2, e1) == Seq(e1, e2))
  }

  test("job ordering puts scheduled operation ahead of stopping a job") {
    assertSortedOrder(ScheduleOperation(UpdateVariable("FOO", AgentKind.Observer, 0, "", "abc"),
      "abc", 1), StopJob("a", 0))
  }

  test("job ordering puts scheduled operation job ahead of adding a job") {
    assertSortedOrder(StopJob("a", 1), AddJob(dummyJob, "abc", 0, 0))
  }

  test("job ordering puts the oldest job stop first"){
    assertSortedOrder(StopJob("a", 1), StopJob("a", 2))
  }

  test("job ordering puts the oldest add job first"){
    assertSortedOrder(AddJob(dummyJob, "abc", 0, 0), AddJob(dummyJob, "abc", 0, 1))
  }

  test("job ordering ranks adding a job and adding a monitor by time"){
    assertSortedOrder(AddMonitor(dummyJob, "abc", 0), AddJob(dummyJob, "abc", 0, 1))
    assertSortedOrder(AddJob(dummyJob, "abc", 0, 0), AddMonitor(dummyJob, "abc", 1))
  }

  test("job ordering puts run job behind adding a job"){
    assertSortedOrder(AddJob(dummyJob, "abc", 0, 0), RunJob(dummyJob, "abc", 0, 1))
  }

  // NOTE: if the job thread is ever expanded to include secondary or intermittent
  // jobs, this ordering should be tweaked
  test("job ordering puts the oldest run job first"){
    assertSortedOrder(RunJob(dummyJob, "abc", 0, 0), RunJob(dummyJob, "abc", 0, 1))
  }

  test("job ordering puts a clear all ahead of an operation") {
    assertSortedOrder(ClearAll(0), ScheduleOperation(UpdateVariable("FOO", AgentKind.Observer, 1, "abc", "def"), "update-foo", 0))
  }

  test("job ordering puts an old monitor update ahead of RunJob") {
    assertSortedOrder(RunMonitors(Map.empty[String, SuspendableJob], 0), RunJob(dummyJob, "abc", 0, 1))
  }

  test("job ordering puts an old job ahead of monitor updates") {
    assertSortedOrder(RunMonitors(Map.empty[String, SuspendableJob], 0), RunJob(dummyJob, "abc", 0, 1))
  }

  class DummyJob extends SuspendableJob {
    var result: AnyRef = null
    def returning(a: AnyRef): DummyJob = { result = a; this }
    def runResult(): AnyRef = {
      runCount += 1
      whileRunning()
      result
    }
    def scheduledBy(s: ApiJobScheduler) = {}
    // this is used to simulate another message being enqueued while this job is running
    var whileRunning: () => Unit = { () => }
    def onNextRun(f: () => Unit): DummyJob = { whileRunning = f; this }
    var repeat: Boolean = false
    def keepRepeating(): DummyJob = { repeat = true; this }
    var runCount = 0
    var intact = true
    def wasRun = runCount > 0
    def runFor(steps: Int): Either[ModelUpdate, Option[SuspendableJob]] = {
      runCount += 1
      whileRunning()
      if (repeat) Right(Some(this))
      else        Right(None)
    }
    def haltOnNextRun(haltAll: Boolean, andAlso: () => Unit = whileRunning): Unit = {
      whileRunning = { () =>
        andAlso()
        throw new HaltException(haltAll)
      }
    }
    var tag: String = ""
    def tag(t: String) = {
      tag = t
      this
    }
  }

  trait Helper extends Inside {
    val subject = new SimpleScheduler()
    def firstTask = subject.queue.peek()
    val DummyOneRunJob = new DummyJob()
    val DummyKeepRunningJob = new DummyJob().keepRepeating()
    val DummyErrorJob = new DummyJob().onNextRun(() => throw new RuntimeException("error!"))
    val DummyUpdateJob = new DummyJob() {
      override def runFor(steps: Int): Either[ModelUpdate, Option[SuspendableJob]] =
        Left(TicksStarted)
    }
    val resultJob = new DummyJob().returning(Double.box(123))
    def scheduleOperation(op: ModelOperation): String = {
      val createdTask = subject.createOperation(op)
      subject.queueTask(createdTask)
      createdTask.tag
    }
    def scheduleOperation(op: () => Unit): String = {
      val createdTask = subject.createOperation(op)
      subject.queueTask(createdTask)
      createdTask.tag
    }
    def scheduleJob(j: SuspendableJob): String = scheduleJob(j, 0)
    def scheduleJob(j: SuspendableJob, interval: Long): String = {
      val createdTask = subject.createJob(j, interval)
      subject.queueTask(createdTask)
      createdTask.tag
    }
    def assertUpdate[U](pf: PartialFunction[ModelUpdate, U]): U = {
      assert(! subject.updates.isEmpty)
      inside(subject.updates.peek)(pf)
    }
    def assertNextUpdate[U](pf: PartialFunction[ModelUpdate, U]): U = {
      val r = assertUpdate[U](pf)
      subject.updates.poll
      r
    }
    def assertHasHalted(tags: String*): Unit = {
      tags.foreach { tag =>
        assertNextUpdate { case JobHalted(t) => assertResult(tag)(t) }
      }
    }
    def runTasks(i: Int): Unit = {
      (1 to i).foreach { i => subject.stepTask() }
    }
    def elapse(i: Int): Unit = {
      subject.setTime += i
    }
  }

  test("adding a job schedules the job to be run") { new Helper {
    val jobTag = scheduleJob(DummyOneRunJob)
    inside(firstTask) { case AddJob(_, t, _, time) => assert(t == jobTag) }
  } }

  test("jobs can be scheduled with a delay interval between them") { new Helper {
    val jobTag = scheduleJob(DummyKeepRunningJob, 100)
    inside(firstTask) { case AddJob(_, t, delay, _) => assert(delay == 100) }
  } }

  test("scheduling an operation schedules it for run") { new Helper {
    val jobTag = scheduleOperation({ () => })
    inside(firstTask) { case ScheduleArbitraryAction(_, t, time) => assert(t == jobTag) }
  } }

  test("stopping a job schedules a job stop") { new Helper {
    subject.stopJob("abc")
    inside(firstTask) { case StopJob(cancelTag, _) => assert(cancelTag == "abc") }
  } }

  test("running a job addition causes a scheduled job to be added") { new Helper {
    val jobTag = scheduleJob(DummyOneRunJob)
    runTasks(1)
    inside(firstTask) { case RunJob(j, t, _, time) =>
      assert(j == DummyOneRunJob)
      assert(t == jobTag)
    }
  } }

  test("Running a job executes the job") { new Helper {
    scheduleJob(DummyOneRunJob)
    runTasks(2)
    assert(DummyOneRunJob.wasRun)
    assert(subject.queue.isEmpty)
  } }

  test("a job may continue to run by returning a job to continue running") { new Helper {
    val tag = scheduleJob(DummyKeepRunningJob)
    runTasks(2)
    assert(! subject.queue.isEmpty)
    inside(subject.queue.peek) { case RunJob(job, t, _, _) =>
      assert(job == DummyKeepRunningJob)
      assert(t == tag)
    }
  } }

  test("if a job stop is processed before the job is added, the job is never added"){ new Helper {
    val jobTag = scheduleJob(DummyOneRunJob)
    val stopJob = subject.stopJob(jobTag)
    subject.queue.add(subject.queue.poll()) // swap the order of the job add and job stop
    runTasks(2)
    assert(subject.queue.isEmpty)
    assert(! DummyOneRunJob.wasRun)
  } }

  test("a job stop prevents a scheduled job from being run") { new Helper {
    val jobTag = scheduleJob(DummyOneRunJob)
    val stopJob = subject.stopJob(jobTag)
    runTasks(3)
    assert(! DummyOneRunJob.wasRun)
  } }

  test("a stopped job will be run until it finishes intact before stopping") { new Helper {
    val job = DummyKeepRunningJob
    val jobTag = scheduleJob(job)
    job.intact = false
    runTasks(2) // runs once here
    subject.stopJob(jobTag)
    runTasks(2) // runs a second time here
    job.intact = true
    runTasks(2) // runs a third time (and finishes intact) here
    assert(job.runCount == 3)
    assert(subject.queue.isEmpty)
  } }

  test("A scheduled operation is run in turn") { new Helper {
    var ranOp = false
    scheduleOperation({ () => ranOp = true })
    runTasks(1)
    assert(ranOp)
  } }

  test("sends an update when a job finishes") { new Helper {
    val jobTag = scheduleJob(DummyOneRunJob)
    runTasks(2)
    assert(! subject.updates.isEmpty)
    assertUpdate { case JobDone(t) => assertResult(jobTag)(t) }
  } }

  test("sends an update when a job errors") { new Helper {
    val jobTag = scheduleJob(DummyErrorJob)
    runTasks(2)
    assertUpdate { case JobErrored(t, _) => assertResult(jobTag)(t) }
  } }

  test("sends an update when an operation completes") { new Helper {
    val jobTag = scheduleOperation({() => })
    runTasks(1)
    assertUpdate { case JobDone(t) => assertResult(jobTag)(t) }
  } }

  test("sends an update when an operation errors") { new Helper {
    val jobTag = scheduleOperation({ () => throw new RuntimeException("error!") })
    runTasks(1)
    assertUpdate { case JobErrored(t, _) => assertResult(jobTag)(t) }
  } }

  test("sends an update when a job is stopped") { new Helper {
    val jobTag = scheduleJob(DummyKeepRunningJob)
    subject.stopJob(jobTag)
    runTasks(3)
    assertUpdate { case JobDone(t) => assertResult(jobTag)(t) }
  } }

  test("sends an update when a job is stopped before being run") { new Helper {
    val jobTag = scheduleJob(DummyKeepRunningJob)
    subject.stopJob(jobTag)
    subject.queue.add(subject.queue.poll()) // swap the order of the job add and job stop
    runTasks(2)
    assertUpdate { case JobDone(t) => assertResult(jobTag)(t) }
  } }

  test("sends monitor updates") { new Helper {
    subject.registerMonitor("abc", resultJob)
    runTasks(2)
    assertUpdate { case MonitorsUpdate(values, _) => assertResult(Try(Double.box(123)))(values("abc")) }
  } }

  test("doesn't re-run monitors unless there's new data") { new Helper {
    subject.registerMonitor("abc", resultJob)
    runTasks(3)
    assert(resultJob.wasRun)
  } }

  test("can pause between running a job for an interval") { new Helper {
    scheduleJob(DummyKeepRunningJob, 100)
    runTasks(2)
    assert(subject.queue.isEmpty)
    elapse(50)
    runTasks(1)
    assert(subject.queue.isEmpty)
    elapse(50)
    runTasks(1)
    assert(subject.queue.isEmpty)
    elapse(50)
    runTasks(1)
    assert(!subject.queue.isEmpty)
    runTasks(1)
    assert(DummyKeepRunningJob.runCount == 2)
  } }

  test("jobs can be canceled while suspended") { new Helper {
    val jobTag = scheduleJob(DummyKeepRunningJob, 100)
    runTasks(2)
    assert(subject.queue.isEmpty)
    subject.stopJob(jobTag)
    runTasks(1)
    elapse(101)
    runTasks(1)
    assert(subject.queue.isEmpty)
    assert(DummyKeepRunningJob.runCount == 1)
  } }

  test("when a job throws a HaltException, sends message for that job") { new Helper {
    val jobTag = scheduleJob(DummyKeepRunningJob, 0)
    runTasks(2)
    DummyKeepRunningJob.haltOnNextRun(true)
    runTasks(1)
    assertUpdate { case JobHalted(t) => assertResult(jobTag)(t) }
  } }

  test("when a job throws a HaltException without haltAll, only that job is cancelled") { new Helper {
    val haltingJob = new DummyJob().keepRepeating()
    val tag1 = scheduleJob(DummyKeepRunningJob, 0)
    val tag2 = scheduleJob(haltingJob, 0)
    runTasks(4)
    haltingJob.haltOnNextRun(false)
    runTasks(2)
    assertHasHalted(tag2)
    assert(subject.updates.isEmpty)
  } }

  test("when a job throws a HaltException with haltAll, other active jobs are cleared") { new Helper {
    val haltingJob = new DummyJob().keepRepeating()
    val tag1 = scheduleJob(DummyKeepRunningJob, 0)
    val tag2 = scheduleJob(haltingJob, 0)
    runTasks(4)
    haltingJob.haltOnNextRun(true)
    runTasks(2)
    assertHasHalted(tag2, tag1)
  } }

  test("when a job throws a HaltException with haltAll, suspended jobs are cleared") { new Helper {
    val haltingJob = new DummyJob().keepRepeating()
    val tag1 = scheduleJob(DummyKeepRunningJob, 100)
    val tag2 = scheduleJob(haltingJob, 0)
    runTasks(4)
    haltingJob.haltOnNextRun(true)
    runTasks(1)
    assertHasHalted(tag2, tag1)
  } }

  test("when a job throws a HaltException with haltAll, monitors are cleared") { new Helper {
    val haltingJob = new DummyJob().keepRepeating()
    val tag1 = scheduleJob(haltingJob, 0)
    subject.registerMonitor("abc", resultJob)
    runTasks(4)
    haltingJob.haltOnNextRun(true)
    runTasks(1)
    subject.updates.clear()
    elapse(101)
    runTasks(1)
    assert(subject.updates.isEmpty)
  } }

  test("when a job throws a HaltException, pending jobs are halted") { new Helper {
    val haltingJob = new DummyJob().keepRepeating()
    val tag1 = scheduleJob(haltingJob, 0)
    runTasks(1)
    var tag2 = ""
    haltingJob.haltOnNextRun(true, { () => tag2 = scheduleJob(DummyKeepRunningJob, 0) })
    runTasks(1)
    assertHasHalted(tag1, tag2)
  } }

  test("after halt clears jobs, resets haltRequested to false") { new Helper {
    val haltingJob = new DummyJob().keepRepeating()
    val tag1 = scheduleJob(haltingJob, 0)
    runTasks(1)
    subject.halt()
    haltingJob.haltOnNextRun(true)
    runTasks(1)
    assertHasHalted(tag1)
    assert(! subject.haltRequested)
  } }

  test("when a monitor throws a HaltException, stops jobs") { new Helper {
    val haltingMonitor = new DummyJob().returning(Double.box(123))
    subject.registerMonitor("abc", haltingMonitor)
    runTasks(1)
    haltingMonitor.haltOnNextRun(true)
    runTasks(2)
    assert(subject.queue.isEmpty)
  } }

  test("operations are run by model operations") { new Helper {
    val update = new UpdateVariable("FOO", AgentKind.Observer, 0, Double.box(123), Double.box(456))
    scheduleOperation(update)
    runTasks(3)
    assert(subject.processedOperations.contains(update))
  } }

  test("supports an operation to clear all jobs and pending jobs") { new Helper {
    subject.registerMonitor("abc", resultJob)
    scheduleJob(DummyKeepRunningJob, 100)
    runTasks(4)
    subject.clearJobsAndMonitors()
    runTasks(2)
    assert(subject.queue.isEmpty)
    elapse(101)
    runTasks(1)
    assert(subject.queue.isEmpty)
  } }

  test("after a job asks for an update to be sent, the update is sent and the job is removed") { new Helper {
    val jobTag = scheduleJob(DummyUpdateJob)
    runTasks(2)
    assert(subject.queue.isEmpty)
    assertUpdate { case TicksStarted => }
  } }
}
