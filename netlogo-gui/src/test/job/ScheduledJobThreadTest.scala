// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.job

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.LinkedBlockingQueue
import java.util.{ Collections, ArrayList }

import org.nlogo.internalapi.{ ModelAction, UpdateInterfaceGlobal, AddProcedureRun,
  JobDone, JobErrored, JobHalted, JobScheduler => ApiJobScheduler, ModelUpdate, MonitorsUpdate,
  StopProcedure, SuspendableJob }
import org.nlogo.nvm.HaltException

import org.scalatest.{ FunSuite, Inside }

import scala.collection.JavaConverters._
import scala.util.Try

import ScheduledJobThread._

class ScheduledJobThreadTest extends FunSuite {

  def sortEvents(es: ScheduledEvent*): Seq[ScheduledEvent] = {
    val a = new ArrayList[ScheduledEvent](es.length)
    es.foreach(a.add)
    Collections.sort(a, ScheduledJobThread.PriorityOrder)
    a.asScala
  }
  val dummyJob = new DummyJob()

  def assertSortedOrder(e1: ScheduledEvent, e2: ScheduledEvent): Unit = {
    assert(sortEvents(e1, e2) == Seq(e1, e2))
    assert(sortEvents(e2, e1) == Seq(e1, e2))
  }

  test("job ordering puts scheduled operation ahead of stopping a job") {
    assertSortedOrder(ScheduleOperation({() => }, "abc", 1), StopJob("a", 0))
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

  test("job ordering puts an old monitor update ahead of RunJob") {
    assertSortedOrder(RunMonitors(Map.empty[String, SuspendableJob], 0), RunJob(dummyJob, "abc", 0, 1))
  }

  test("job ordering puts an old job ahead of monitor updates") {
    assertSortedOrder(RunMonitors(Map.empty[String, SuspendableJob], 0), RunJob(dummyJob, "abc", 0, 1))
  }

  class Subject extends JobScheduler {
    override def timeout = 10
    val queue = new LinkedBlockingQueue[ScheduledEvent]
    val updates = new LinkedBlockingQueue[ModelUpdate]
    def die(): Unit = {}
    var setTime: Long = 0
    override def currentTime = setTime
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
    def runFor(steps: Int): Option[SuspendableJob] = {
      runCount += 1
      whileRunning()
      if (repeat) Some(this)
      else        None
    }
    def haltOnNextRun(haltAll: Boolean, andAlso: () => Unit = whileRunning): Unit = {
      whileRunning = { () =>
        andAlso()
        throw new HaltException(haltAll)
      }
    }
  }

  trait Helper extends Inside {
    val subject = new Subject()
    def firstEvent = subject.queue.peek()
    val DummyOneRunJob = new DummyJob()
    val DummyKeepRunningJob = new DummyJob().keepRepeating()
    val DummyErrorJob = new DummyJob().onNextRun(() => throw new RuntimeException("error!"))
    val resultJob = new DummyJob().returning(Double.box(123))
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
    def runEvents(i: Int): Unit = {
      (1 to i).foreach { i => subject.runEvent() }
    }
    def elapse(i: Int): Unit = {
      subject.setTime += i
    }
  }

  test("adding a job schedules the job to be run") { new Helper {
    val jobTag = subject.scheduleJob(DummyOneRunJob)
    inside(firstEvent) { case AddJob(_, t, _, time) => assert(t == jobTag) }
  } }

  test("jobs can be scheduled with a delay interval between them") { new Helper {
    val jobTag = subject.scheduleJob(DummyKeepRunningJob, 100)
    inside(firstEvent) { case AddJob(_, t, delay, _) => assert(delay == 100) }
  } }

  test("scheduling an operation schedules it for run") { new Helper {
    val jobTag = subject.scheduleOperation({ () => })
    inside(firstEvent) { case ScheduleOperation(_, t, time) => assert(t == jobTag) }
  } }

  test("stopping a job schedules a job stop") { new Helper {
    subject.stopJob("abc")
    inside(firstEvent) { case StopJob(cancelTag, _) => assert(cancelTag == "abc") }
  } }

  test("running a job addition causes a scheduled job to be added") { new Helper {
    val jobTag = subject.scheduleJob(DummyOneRunJob)
    subject.runEvent()
    inside(firstEvent) { case RunJob(j, t, _, time) =>
      assert(j == DummyOneRunJob)
      assert(t == jobTag)
    }
  } }

  test("Running a job executes the job") { new Helper {
    subject.scheduleJob(DummyOneRunJob)
    runEvents(2)
    assert(DummyOneRunJob.wasRun)
    assert(subject.queue.isEmpty)
  } }

  test("a job may continue to run by returning a job to continue running") { new Helper {
    val tag = subject.scheduleJob(DummyKeepRunningJob)
    runEvents(2)
    assert(! subject.queue.isEmpty)
    inside(subject.queue.peek) { case RunJob(job, t, _, _) =>
      assert(job == DummyKeepRunningJob)
      assert(t == tag)
    }
  } }

  test("if a job stop is processed before the job is added, the job is never added"){ new Helper {
    val jobTag = subject.scheduleJob(DummyOneRunJob)
    val stopJob = subject.stopJob(jobTag)
    subject.queue.add(subject.queue.poll()) // swap the order of the job add and job stop
    runEvents(2)
    assert(subject.queue.isEmpty)
    assert(! DummyOneRunJob.wasRun)
  } }

  test("a job stop prevents a scheduled job from being run") { new Helper {
    val jobTag = subject.scheduleJob(DummyOneRunJob)
    val stopJob = subject.stopJob(jobTag)
    runEvents(3)
    assert(! DummyOneRunJob.wasRun)
  } }

  test("a stopped job will be run until it finishes intact before stopping") { new Helper {
    val job = DummyKeepRunningJob
    val jobTag = subject.scheduleJob(job)
    job.intact = false
    runEvents(2) // runs once here
    subject.stopJob(jobTag)
    runEvents(2) // runs a second time here
    job.intact = true
    runEvents(2) // runs a third time (and finishes intact) here
    assert(job.runCount == 3)
    assert(subject.queue.isEmpty)
  } }

  test("A scheduled operation is run in turn") { new Helper {
    var ranOp = false
    subject.scheduleOperation({ () => ranOp = true })
    subject.runEvent()
    assert(ranOp)
  } }

  test("sends an update when a job finishes") { new Helper {
    val jobTag = subject.scheduleJob(DummyOneRunJob)
    runEvents(2)
    assert(! subject.updates.isEmpty)
    assertUpdate { case JobDone(t) => assertResult(jobTag)(t) }
  } }

  test("sends an update when a job errors") { new Helper {
    val jobTag = subject.scheduleJob(DummyErrorJob)
    runEvents(2)
    assertUpdate { case JobErrored(t, _) => assertResult(jobTag)(t) }
  } }

  test("sends an update when an operation completes") { new Helper {
    val jobTag = subject.scheduleOperation({() => })
    subject.runEvent()
    assertUpdate { case JobDone(t) => assertResult(jobTag)(t) }
  } }

  test("sends an update when an operation errors") { new Helper {
    val jobTag = subject.scheduleOperation({ () => throw new RuntimeException("error!") })
    subject.runEvent()
    assertUpdate { case JobErrored(t, _) => assertResult(jobTag)(t) }
  } }

  test("sends an update when a job is stopped") { new Helper {
    val jobTag = subject.scheduleJob(DummyKeepRunningJob)
    subject.stopJob(jobTag)
    runEvents(3)
    assertUpdate { case JobDone(t) => assertResult(jobTag)(t) }
  } }

  test("sends an update when a job is stopped before being run") { new Helper {
    val jobTag = subject.scheduleJob(DummyKeepRunningJob)
    subject.stopJob(jobTag)
    subject.queue.add(subject.queue.poll()) // swap the order of the job add and job stop
    runEvents(2)
    assertUpdate { case JobDone(t) => assertResult(jobTag)(t) }
  } }

  test("sends monitor updates") { new Helper {
    subject.registerMonitor("abc", resultJob)
    runEvents(2)
    assertUpdate { case MonitorsUpdate(values, _) => assertResult(Try(Double.box(123)))(values("abc")) }
  } }

  test("doesn't re-run monitors unless there's new data") { new Helper {
    subject.registerMonitor("abc", resultJob)
    runEvents(3)
    assert(resultJob.wasRun)
  } }

  test("can pause between running a job for an interval") { new Helper {
    subject.scheduleJob(DummyKeepRunningJob, 100)
    runEvents(2)
    assert(subject.queue.isEmpty)
    elapse(50)
    runEvents(1)
    assert(subject.queue.isEmpty)
    elapse(50)
    runEvents(1)
    assert(subject.queue.isEmpty)
    elapse(50)
    runEvents(1)
    assert(!subject.queue.isEmpty)
    runEvents(1)
    assert(DummyKeepRunningJob.runCount == 2)
  } }

  test("jobs can be canceled while suspended") { new Helper {
    val jobTag = subject.scheduleJob(DummyKeepRunningJob, 100)
    runEvents(2)
    assert(subject.queue.isEmpty)
    subject.stopJob(jobTag)
    runEvents(1)
    elapse(101)
    runEvents(1)
    assert(subject.queue.isEmpty)
    assert(DummyKeepRunningJob.runCount == 1)
  } }

  test("when a job throws a HaltException, sends message for that job") { new Helper {
    val jobTag = subject.scheduleJob(DummyKeepRunningJob, 0)
    runEvents(2)
    DummyKeepRunningJob.haltOnNextRun(true)
    runEvents(1)
    assertUpdate { case JobHalted(t) => assertResult(jobTag)(t) }
  } }

  test("when a job throws a HaltException without haltAll, only that job is cancelled") { new Helper {
    val haltingJob = new DummyJob().keepRepeating()
    val tag1 = subject.scheduleJob(DummyKeepRunningJob, 0)
    val tag2 = subject.scheduleJob(haltingJob, 0)
    runEvents(4)
    haltingJob.haltOnNextRun(false)
    runEvents(2)
    assertHasHalted(tag2)
    assert(subject.updates.isEmpty)
  } }

  test("when a job throws a HaltException with haltAll, other active jobs are cleared") { new Helper {
    val haltingJob = new DummyJob().keepRepeating()
    val tag1 = subject.scheduleJob(DummyKeepRunningJob, 0)
    val tag2 = subject.scheduleJob(haltingJob, 0)
    runEvents(4)
    haltingJob.haltOnNextRun(true)
    runEvents(2)
    assertHasHalted(tag2, tag1)
  } }

  test("when a job throws a HaltException with haltAll, suspended jobs are cleared") { new Helper {
    val haltingJob = new DummyJob().keepRepeating()
    val tag1 = subject.scheduleJob(DummyKeepRunningJob, 100)
    val tag2 = subject.scheduleJob(haltingJob, 0)
    runEvents(4)
    haltingJob.haltOnNextRun(true)
    runEvents(1)
    assertHasHalted(tag2, tag1)
  } }

  test("when a job throws a HaltException with haltAll, monitors are cleared") { new Helper {
    val haltingJob = new DummyJob().keepRepeating()
    val tag1 = subject.scheduleJob(haltingJob, 0)
    subject.registerMonitor("abc", resultJob)
    runEvents(4)
    haltingJob.haltOnNextRun(true)
    runEvents(1)
    subject.updates.clear()
    elapse(101)
    runEvents(1)
    assert(subject.updates.isEmpty)
  } }

  test("when a job throws a HaltException, pending jobs are halted") { new Helper {
    val haltingJob = new DummyJob().keepRepeating()
    val tag1 = subject.scheduleJob(haltingJob, 0)
    runEvents(1)
    var tag2 = ""
    haltingJob.haltOnNextRun(true, { () => tag2 = subject.scheduleJob(DummyKeepRunningJob, 0) })
    runEvents(1)
    assertHasHalted(tag1, tag2)
  } }

  test("after halt clears jobs, resets haltRequested to false") { new Helper {
    val haltingJob = new DummyJob().keepRepeating()
    val tag1 = subject.scheduleJob(haltingJob, 0)
    runEvents(1)
    subject.halt()
    haltingJob.haltOnNextRun(true)
    runEvents(1)
    assertHasHalted(tag1)
    assert(! subject.haltRequested)
  } }

  test("when a monitor throws a HaltException, stops jobs") { new Helper {
    val haltingMonitor = new DummyJob().returning(Double.box(123))
    subject.registerMonitor("abc", haltingMonitor)
    runEvents(1)
    haltingMonitor.haltOnNextRun(true)
    runEvents(2)
    assert(subject.queue.isEmpty)
  } }
}
