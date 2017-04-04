// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.job

import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.LinkedBlockingQueue
import java.util.{ Collections, ArrayList }

import org.nlogo.internalapi.{ ModelAction, UpdateInterfaceGlobal, AddProcedureRun,
  JobDone, JobErrored, ModelUpdate, MonitorsUpdate, StopProcedure, SuspendableJob }

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

  def assertSortedOrder(e1: ScheduledEvent, e2: ScheduledEvent): Unit = {
    assert(sortEvents(e1, e2) == Seq(e1, e2))
    assert(sortEvents(e2, e1) == Seq(e1, e2))
  }

  test("job ordering puts scheduled operation ahead of stopping a job") {
    assertSortedOrder(ScheduleOperation({() => }, "abc", 1), StopJob("a", 0))
  }

  test("job ordering puts scheduled operation job ahead of adding a job") {
    assertSortedOrder(StopJob("a", 1), AddJob(null, "abc", 0, 0))
  }

  test("job ordering puts the oldest job stop first"){
    assertSortedOrder(StopJob("a", 1), StopJob("a", 2))
  }

  test("job ordering puts the oldest add job first"){
    assertSortedOrder(AddJob(null, "abc", 0, 0), AddJob(null, "abc", 0, 1))
  }

  test("job ordering ranks adding a job and adding a monitor by time"){
    assertSortedOrder(AddMonitor(null, "abc", 0), AddJob(null, "abc", 0, 1))
    assertSortedOrder(AddJob(null, "abc", 0, 0), AddMonitor(null, "abc", 1))
  }

  test("job ordering puts run job behind adding a job"){
    assertSortedOrder(AddJob(null, "abc", 0, 0), RunJob(null, "abc", 0, 1))
  }

  // NOTE: if the job thread is ever expanded to include secondary or intermittent
  // jobs, this ordering should be tweaked
  test("job ordering puts the oldest run job first"){
    assertSortedOrder(RunJob(null, "abc", 0, 0), RunJob(null, "abc", 0, 1))
  }

  test("job ordering puts an old monitor update ahead of RunJob") {
    assertSortedOrder(RunMonitors(Map.empty[String, SuspendableJob], 0), RunJob(null, "abc", 0, 1))
  }

  test("job ordering puts an old job ahead of monitor updates") {
    assertSortedOrder(RunMonitors(Map.empty[String, SuspendableJob], 0), RunJob(null, "abc", 0, 1))
  }

  class Subject extends JobScheduler {
    override def timeout = 10
    val queue = new LinkedBlockingQueue[ScheduledEvent]
    val updates = new LinkedBlockingQueue[ModelUpdate]
    def die(): Unit = {}
    var setTime: Long = 0
    override def currentTime = setTime
  }

  trait Helper extends Inside {
    val subject = new Subject()
    def firstEvent = subject.queue.peek()
    var runCount: Int = 0
    def wasRun: Boolean = runCount > 0
    val DummyOneRunJob = new SuspendableJob {
      def runFor(steps: Int): Option[SuspendableJob] = {
        runCount += 1
        None
      }
      def runResult(): AnyRef = null
    }
    val DummyKeepRunningJob = new SuspendableJob {
      def runFor(steps: Int): Option[SuspendableJob] = {
        runCount += 1
        Some(this)
      }
      def runResult(): AnyRef = null
    }
    val DummyErrorJob = new SuspendableJob {
      def runFor(steps: Int): Option[SuspendableJob] = {
        runCount += 1
        throw new RuntimeException("error!")
      }
      def runResult(): AnyRef = null
    }
    var timesRunResultRun = 0
    val resultJob = new SuspendableJob {
      def runFor(steps: Int) = None
      def runResult(): AnyRef = {
        timesRunResultRun += 1
        Double.box(123)
      }
    }
    def assertUpdate[U](pf: PartialFunction[ModelUpdate, U]): U = {
      assert(! subject.updates.isEmpty)
      inside(subject.updates.peek)(pf)
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
    assert(wasRun)
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
    assert(! wasRun)
  } }

  test("a job stop prevents a scheduled job from being run") { new Helper {
    val jobTag = subject.scheduleJob(DummyOneRunJob)
    val stopJob = subject.stopJob(jobTag)
    runEvents(3)
    assert(! wasRun)
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
    assert(timesRunResultRun == 1)
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
    assert(runCount == 2)
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
    assert(runCount == 1)
  } }

  test("supports an operation to clear all existing jobs and monitors") {
    pending
  }

  test("supports halt to immediately stop all tasks in progress") {
    pending
  }
}
