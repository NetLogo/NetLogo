// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.job

import org.nlogo.api.{ JobOwner, LogoException, PeriodicUpdateDelay }
import org.nlogo.nvm.{ ConcurrentJob, Job, JobManagerOwner }
import java.util.{ Collections => JCollections, List => JList, ArrayList => JArrayList }
import org.nlogo.api.Exceptions.{ ignoring, handling }

object JobThread {

  // too little limits recursion in user code, too much may cause OutOfMemoryErrors on Windows
  // and/or Linux (even if there's plenty of extra heap).
  private val defaultStackSize = 8   // megabytes

  // allow override through property
  def stackSize: Int =
    java.lang.Integer.getInteger("org.nlogo.stackSize", defaultStackSize)
}

class JobThread(manager: JobManager, owner: JobManagerOwner, lock: AnyRef)
extends Thread(null, null, "JobThread", JobThread.stackSize * 1024 * 1024) {

  val primaryJobs = JCollections.synchronizedList(new JArrayList[Job])
  val secondaryJobs = JCollections.synchronizedList(new JArrayList[Job])

  @volatile private var dying = false

  /// these are package-visible so they can be used by JobManager too

  val turtleForeverButtonJobs =
    JCollections.synchronizedList(new JArrayList[ConcurrentJob])
  val linkForeverButtonJobs =
    JCollections.synchronizedList(new JArrayList[ConcurrentJob])
  var lastSecondaryRun = 0L
  val newJobsCondition = new AnyRef
  var isTimeToRunSecondaryJobs = false
  private var activeButton: JobOwner = null

  private var lastSecondaryRunDuration = 0L

  // the GUI should be higher priority than us - ST 1/13/05
  setPriority(Thread.NORM_PRIORITY - 1)
  start()

  @throws(classOf[InterruptedException])
  def die() {
    // In the days of applets, this used to ignore NPE and AccessControlException
    // when thrown by `setPriority`. In the modern Java 8 world
    // (where applets are dead), I think it would be better to go ahead
    // and error if this fails, since it indicates a serious
    // issue we should probably account for - RG 4/28/16
    // I don't understand why this line should be necessary, but without it, this method runs
    // very slowly (a noticeable fraction of a second) on Windows.  Or maybe it's not a Windows
    // thing, but a single-CPU thing...? - ST 1/19/05
    setPriority(Thread.MAX_PRIORITY)
    dying = true
    newJobsCondition.synchronized {
      newJobsCondition.notifyAll()
    }
    join()
  }

  override def run() {
    handling(classOf[RuntimeException]) {
      while (!dying) {
        compact(primaryJobs)
        runPrimaryJobs()
        maybeRunSecondaryJobs()
        // Note that we must synchronize on newJobsCondition before calling isEmpty, since
        // otherwise a job could get added after the empty check but before we sleep - ST 8/11/03
        newJobsCondition.synchronized {
          if (primaryJobs.isEmpty)
            ignoring(classOf[InterruptedException]) {
              // only sleep for a short time, since there may still
              // be secondary jobs that need attention - ST 8/10/03
              newJobsCondition.wait(PeriodicUpdateDelay.DelayInMilliseconds)
            } } } }
  }

  def maybeRunSecondaryJobs() {
    // our owner will tell us when it's time - ST 8/10/03
    if (isTimeToRunSecondaryJobs) {
      val now = System.currentTimeMillis()
      // if the secondary jobs take a long time to run, we don't want to run them whenever our owner
      // tells us to; we want to take the time they take to run into account too, so we don't hog
      // the CPU; hence the lastSecondaryRunDuration variable - ST 8/10/03
      if (now - lastSecondaryRun >
          PeriodicUpdateDelay.DelayInMilliseconds / 2 + lastSecondaryRunDuration) {
        compact(secondaryJobs)
        runSecondaryJobs()
        isTimeToRunSecondaryJobs = false
        lastSecondaryRun = System.currentTimeMillis()
        lastSecondaryRunDuration = lastSecondaryRun - now
        owner.periodicUpdate()
      }
    }
  }

  // this and runSecondaryJobs() differ only in a few details
  private def runPrimaryJobs() {
    var i = 0
    while(i < primaryJobs.size) {
      val job = primaryJobs.get(i)
      if (job.state != Job.RUNNING) {
        if (job.topLevelProcedure != null && (job.owner eq activeButton))
          activeButton = null
        primaryJobs.set(i, null)
        if (job.isTurtleForeverButtonJob)
          turtleForeverButtonJobs.remove(job)
        if (job.isLinkForeverButtonJob)
          linkForeverButtonJobs.remove(job)
        job.state = Job.REMOVED
        if (job.topLevelProcedure != null) {
          owner.updateDisplay(false, false)
          job.synchronized {
            job.notifyAll()
          }
          owner.ownerFinished(job.owner)
        }
      }
      else {
        var skip = false
        if (job.owner.isButton && job.topLevelProcedure != null) {
          if (activeButton != null) {
            if (job.owner != activeButton)
              skip = true
          }
          else
            activeButton = job.owner
        }
        if(!skip)
          try lock.synchronized { job.step() }
          catch {
            case ex: LogoException =>
              job.result = ex
              manager.finishJobs(primaryJobs, job.owner)
            case ex: RuntimeException =>
              job.result = ex
              manager.finishJobs(primaryJobs, job.owner)
          }
        if (job.buttonTurnIsOver) {
          activeButton = null
          job.buttonTurnIsOver = false
        }
      }
      i += 1
    }
  }

  // This and runPrimaryJobs() differ only in two details:
  //  - secondary jobs do not cause display updates
  //  - here we don't need all the buttonTurnIsOver/activeButton stuff that makes
  //    buttons take turns
  private def runSecondaryJobs() {
    var i = 0
    while(i < secondaryJobs.size) {
      val job = secondaryJobs.get(i)
      if (job.state != Job.RUNNING) {
        secondaryJobs.set(i, null)
        job.state = Job.REMOVED
        if (job.topLevelProcedure != null) {
          job.synchronized { job.notifyAll() }
          owner.ownerFinished(job.owner)
        }
      }
      else
        try lock.synchronized { job.step() }
        catch {
          case ex: LogoException =>
            job.result = ex
            manager.finishJobs(primaryJobs, job.owner)
          case ex: RuntimeException =>
            job.result = ex
            manager.finishJobs(primaryJobs, job.owner)
        }
      i += 1
    }
  }

  /// helpers

  private def compact(list: JList[Job]) {
    list.synchronized {
      val iter = list.iterator
      while(iter.hasNext)
      if(iter.next() == null)
        iter.remove()
    }
  }

}
