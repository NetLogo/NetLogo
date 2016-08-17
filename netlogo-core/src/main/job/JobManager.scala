// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.job

import org.nlogo.nvm.{ExclusiveJob, ConcurrentJob, Procedure, Job, JobManagerOwner, Workspace}
import org.nlogo.core.AgentKind
import org.nlogo.api.{JobOwner, LogoException}
import org.nlogo.agent.{Agent, Turtle, Link, AgentSet, World}
import java.util.List
import org.nlogo.api.Exceptions.ignoring
import collection.JavaConverters._

class JobManager(jobManagerOwner: JobManagerOwner,
                 private val world: World, lock: Object) extends org.nlogo.nvm.JobManagerInterface {

  private val thread = new JobThread(this, jobManagerOwner, lock)

  /// misc public methods
  def isInterrupted = thread.isInterrupted()
  def interrupt() { thread.interrupt() }
  @throws(classOf[InterruptedException])
  def die() {thread.die()}
  def timeToRunSecondaryJobs() { thread.isTimeToRunSecondaryJobs = true }
  def maybeRunSecondaryJobs() { thread.maybeRunSecondaryJobs() }
  def anyPrimaryJobs = !thread.primaryJobs.isEmpty

  /// public methods for adding jobs

  def addJob(job: Job, waitForCompletion: Boolean) {
    if (waitForCompletion) { add(job, thread.primaryJobs); waitFor(job, false) }
    else add(job, thread.primaryJobs)
  }

  def makeConcurrentJob(owner: JobOwner, agentSet: AgentSet, workspace: Workspace, procedure: Procedure): Job =
    new ConcurrentJob(owner, agentSet, procedure, 0, null, workspace, owner.random)

  @throws(classOf[LogoException])
  def callReporterProcedure(owner: JobOwner, agentSet: AgentSet, workspace: Workspace, procedure: Procedure): Object =
    new ExclusiveJob(owner, agentSet, procedure, 0, null, workspace, owner.random).callReporterProcedure()

  def addReporterJobAndWait(owner: JobOwner, agentSet: AgentSet, workspace: Workspace, procedure: Procedure): Object = {
    val job = new ConcurrentJob(owner, agentSet, procedure, 0, null, workspace, owner.random)
    add(job, thread.primaryJobs)
    waitFor(job, false)
    job.result
  }

  def addJobFromJobThread(job: Job) {thread.primaryJobs.add(job)}
  def addJob(owner: JobOwner, agents: AgentSet, workspace: Workspace, procedure: Procedure) {
    add(new ConcurrentJob(owner, agents, procedure, 0, null, workspace, owner.random),
        thread.primaryJobs)
  }
  def addSecondaryJob(owner: JobOwner, agents: AgentSet, workspace: Workspace, procedure: Procedure) {
    // we only allow one secondary job per owner -- this is so MonitorWidgets
    // don't wind up with multiple jobs -- it's a bit of a kludge - ST 9/19/01
    val found = thread.secondaryJobs.synchronized {
      thread.secondaryJobs.asScala.exists{ s => s != null && s.owner == owner && s.state == Job.RUNNING }
    }
    if(!found)
      add(new ConcurrentJob(owner, agents, procedure, 0, null, workspace, owner.random),
          thread.secondaryJobs)
  }

  /// private methods for adding a job

  private def add(job: Job, jobs: List[Job]) {
    jobs.add(job)
    if (job.isTurtleForeverButtonJob) thread.turtleForeverButtonJobs.add(job.asInstanceOf[ConcurrentJob])
    if (job.isLinkForeverButtonJob) thread.linkForeverButtonJobs.add(job.asInstanceOf[ConcurrentJob])
    if (job.topLevelProcedure != null)
      thread.newJobsCondition.synchronized {thread.newJobsCondition.notifyAll()}
  }

  /// public method for adding new turtles to existing jobs

  // This is called from the job thread only by such commands as
  // _createbreed, _sprout, _hatch, etc. - ST 8/13/03
  def joinForeverButtons(agent: Agent) {
    agent match {
      case t: Turtle =>
        thread.turtleForeverButtonJobs.synchronized {
          for (job <- thread.turtleForeverButtonJobs.asScala)
            job.newAgentJoining(agent, -1, 0)
        }
      case t: Link =>
        thread.linkForeverButtonJobs.synchronized {
          for (job <- thread.linkForeverButtonJobs.asScala)
            job.newAgentJoining(agent, -1, 0)
        }
      case _ =>
        // this shouldn't happen because patches shouldn't be joining.
        // ev 9/29/06
        throw new IllegalStateException()
    }
  }

  /// public methods for waiting for and/or finishing jobs

  def haltPrimary() {
    finishJobs(thread.primaryJobs, null)
    waitForFinishedJobs(thread.primaryJobs)
  }

  // this is for the resize-world primitive - ST 4/6/09
  def haltNonObserverJobs() {
    val goners = thread.primaryJobs.synchronized {
      thread.primaryJobs.asScala.filter {j => j != null && j.agentset.kind != AgentKind.Observer}
    }.asJava
    finishJobs(goners, null)
    waitForFinishedJobs(goners)
  }

  def finishJobs(owner: JobOwner) {finishJobs(thread.primaryJobs, owner)}

  def finishSecondaryJobs(owner: JobOwner) {
    finishJobs(thread.secondaryJobs, owner)
    thread.lastSecondaryRun = 0
    timeToRunSecondaryJobs()
  }

  def haltSecondary() {
    finishJobs(thread.secondaryJobs, null)
    thread.lastSecondaryRun = 0
    timeToRunSecondaryJobs()
    waitForFinishedJobs(thread.secondaryJobs)
  }

  def stoppingJobs(owner: JobOwner) {
    thread.primaryJobs.synchronized {
      for (job <- thread.primaryJobs.asScala; if (job != null && (owner == null || job.owner == owner))) {
        job.stopping = true
      }
    }
  }

  /// private methods for waiting for and/or finishing jobs

  private def waitFor(job: Job, kill: Boolean) {
    // We could use wait-notify always, but at the cost of introducing
    // lock-acquisition overhead in the run loop, so let's just do
    // this instead.  waitFor() on jobs without lock objects is
    // not used in any loops in engine code, only in response to UI
    // actions, so it doesn't need to be super fast.
    // - ST 12/17/02, 3/19/03
    while (job.state != Job.REMOVED && thread.isAlive) {
      if (kill) {
        // the job thread might get stuck at any time on an invokeAndWait,
        // so we must keep interrupting it - ST 8/13/03
        thread.interrupt()
        // furthermore, the job thread might be stuck inside an extremely
        // tight loop in an exclusive job setting the comeUpForAir flag
        // is the only way to get it unstuck - ST 1/10/07
        world.comeUpForAir = true
      }
      thread.isTimeToRunSecondaryJobs = true
      ignoring(classOf[InterruptedException]) { job.synchronized {job.wait(50)} }
    }
    if (job.result.isInstanceOf[Exception] && !job.result.isInstanceOf[LogoException])
      throw job.result.asInstanceOf[Exception]
  }

  def finishJobs(jobs: List[Job], owner: JobOwner) {
    jobs.synchronized {
      for {
        job <- jobs.asScala
        if (job != null && (owner == null || job.owner == owner))
      } job.finish()
    }
  }

  private def waitForFinishedJobs(jobs: List[Job]): Unit = {
    // We synchronize on the jobs list only long enough to
    // grab the first job that we need to wait for.  We can't
    // just hang onto the lock, because the run() loop needs
    // it in order to keep from getting stuck.
    // This is inefficient because once that we've waited for the
    // job, we go all the way back to the beginning of the jobs list
    // to scan for the next job to wait for.  But I think this is
    // unlikely to be a problem in practice, especially since this
    // method is never called from a loop.
    // - ST 12/17/02
    while (thread.isAlive) {
      val jobToWaitFor = jobs.synchronized {jobs.asScala.find(_ != null)}
      jobToWaitFor match {
        case Some(j) => waitFor(j, true)
        case _ => return // ick
      }
    }
  }
}
