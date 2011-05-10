package org.nlogo.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.nlogo.api.JobOwner;
import org.nlogo.api.LogoException;
import org.nlogo.api.PeriodicUpdateDelay;
import org.nlogo.nvm.ConcurrentJob;
import org.nlogo.nvm.Job;
import org.nlogo.nvm.JobManagerInterface;
import org.nlogo.nvm.JobManagerOwner;

final strictfp class JobThread
    extends Thread {

  /// basics & constructor

  private final JobManager manager;
  final JobManagerOwner jobManagerOwner;
  private final Object lock;
  final List<Job> primaryJobs =
      Collections.synchronizedList(new ArrayList<Job>());
  final List<Job> secondaryJobs =
      Collections.synchronizedList(new ArrayList<Job>());
  private volatile boolean dying = false;  // NOPMD pmd doesn't like 'volatile'

  JobThread(JobManager manager, JobManagerOwner jobManagerOwner,
            Object lock) {
    super("JobThread");
    this.manager = manager;
    this.jobManagerOwner = jobManagerOwner;
    this.lock = lock;
    // the GUI should be higher priority than us - ST 1/13/05
    setPriority(Thread.NORM_PRIORITY - 1);
    start();
  }

  /// these are package-visible so they can be used by JobManager too

  final List<ConcurrentJob> turtleForeverButtonJobs =
      Collections.synchronizedList(new ArrayList<ConcurrentJob>());
  final List<ConcurrentJob> linkForeverButtonJobs =
      Collections.synchronizedList(new ArrayList<ConcurrentJob>());
  long lastSecondaryRun = 0;
  final Object newJobsCondition = new Object();
  boolean isTimeToRunSecondaryJobs = false;
  private JobOwner activeButton = null;

  /// all private from here on down except for run()

  private long lastSecondaryRunDuration = 0;

  void die()
      throws InterruptedException {
    try {
      // I don't understand why this next line should be necessary,
      // but without it, this method runs very slowly (a noticeable
      // fraction of a second) on Windows.  Or maybe it's not a Windows
      // thing, but a single-CPU thing...? - ST 1/19/05
      try {
        setPriority(Thread.MAX_PRIORITY);
      } catch (NullPointerException ex) // NOPMD
      {
        // ignore because sometimes setPriority throws NullPointerExceptions for no good reason
        // It was happening to me when using the controlling API with multiple HeadlessWorkspaces,
        // on both Sun Java 1.5.0_07 and IBM Java 1.5.0, both on 64 bit linux machines.  It wasn't
        // happening to me on a 32-bit Java 1.6 computer.  It seems like it might be related to
        // Sun's Java bug #4515956, though it looks like that was fixed before 1.5 so I don't know.
        // Anyway, I don't think there's any harm in ignoring this exception, since no one else was
        // being affected by it anyway.   ~Forrest (8/11/2009)
        org.nlogo.util.Exceptions.ignore(ex);
      }
    } catch (java.security.AccessControlException e) {
      // ignore because we might get this during applet shutdown
      // on MACOS 10.3 ev 12/4/07
      org.nlogo.util.Exceptions.ignore(e);
    }
    dying = true;
    synchronized (newJobsCondition) {
      newJobsCondition.notifyAll();
    }
    join();
  }

  @Override
  public void run() {
    try {
      while (!dying) {
        compact(primaryJobs);
        runPrimaryJobs();
        maybeRunSecondaryJobs();
        // Note that we must synchronize on newJobsCondition before calling
        // isEmpty(), since otherwise a job could get added after the empty
        // check but before we sleep - ST 8/11/03
        synchronized (newJobsCondition) {
          if (primaryJobs.isEmpty()) {
            try {
              // only sleep for a short time, since there may still
              // be secondary jobs that need attention - ST 8/10/03
              newJobsCondition.wait(PeriodicUpdateDelay.PERIODIC_UPDATE_DELAY());
            } catch (InterruptedException ex) {
              // we can be interrupted by our owner
              org.nlogo.util.Exceptions.ignore(ex);
            }
          }
        }
      }
    } catch (RuntimeException ex) {
      org.nlogo.util.Exceptions.handle(ex);
    }
  }

  void maybeRunSecondaryJobs() {
    // our owner will tell us when it's time - ST 8/10/03
    if (isTimeToRunSecondaryJobs) {
      long now = System.currentTimeMillis();
      // if the secondary jobs take a long time to run, we don't want
      // to run them whenever our owner tells us to; we want to take the time
      // they take to run into account too, so we don't hog the CPU;
      // hence the lastSecondaryRunDuration variable - ST 8/10/03
      if (now - lastSecondaryRun >
          PeriodicUpdateDelay.PERIODIC_UPDATE_DELAY() / 2 + lastSecondaryRunDuration) {
        compact(secondaryJobs);
        runSecondaryJobs();
        isTimeToRunSecondaryJobs = false;
        lastSecondaryRun = System.currentTimeMillis();
        lastSecondaryRunDuration = lastSecondaryRun - now;
        jobManagerOwner.periodicUpdate();
      }
    }
  }

  // this and runSecondaryJobs() differ only in a few details
  private void runPrimaryJobs() {
    for (int i = 0; i < primaryJobs.size(); i++) {
      final Job job = primaryJobs.get(i);
      if (job.state != Job.RUNNING) {
        if (job.topLevelProcedure != null && job.owner == activeButton) {
          activeButton = null;
        }
        primaryJobs.set(i, null);
        if (job.isTurtleForeverButtonJob()) {
          turtleForeverButtonJobs.remove(job);
        }
        if (job.isLinkForeverButtonJob()) {
          linkForeverButtonJobs.remove(job);
        }
        job.state = Job.REMOVED;
        if (job.topLevelProcedure != null) {
          jobManagerOwner.updateDisplay(false);
          synchronized (job) {
            job.notifyAll();
          }
          jobManagerOwner.ownerFinished(job.owner);
        }
      } else {
        if (job.owner.isButton() && job.topLevelProcedure != null) {
          if (activeButton != null) {
            if (job.owner != activeButton) {
              continue;
            }
          } else {
            activeButton = job.owner;
          }
        }
        try {
          synchronized (lock) {
            job.step();
          }
        } catch (LogoException ex) {
          job.result = ex;
          manager.finishJobs(primaryJobs, job.owner);
        } catch (RuntimeException ex) {
          job.result = ex;
          manager.finishJobs(primaryJobs, job.owner);
        }
        if (job.buttonTurnIsOver) {
          activeButton = null;
          job.buttonTurnIsOver = false;
        }
      }
    }
  }

  // This and runPrimaryJobs() differ only in two details:
  //  - secondary jobs do not cause display updates
  //  - here we don't need all the buttonTurnIsOver/activeButton stuff that makes
  //    buttons take turns
  private void runSecondaryJobs() {
    for (int i = 0; i < secondaryJobs.size(); i++) {
      final Job job = secondaryJobs.get(i);
      if (job.state != Job.RUNNING) {
        secondaryJobs.set(i, null);
        job.state = Job.REMOVED;
        if (job.topLevelProcedure != null) {
          synchronized (job) {
            job.notifyAll();
          }
          jobManagerOwner.ownerFinished(job.owner);
        }
      } else {
        try {
          synchronized (lock) {
            job.step();
          }
        } catch (LogoException ex) {
          job.result = ex;
          manager.finishJobs(secondaryJobs, job.owner);
        } catch (RuntimeException ex) {
          job.result = ex;
          manager.finishJobs(secondaryJobs, job.owner);
        }
      }
    }
  }

  /// helpers

  private void compact(List<Job> list) {
    synchronized (list) {
      for (Iterator<Job> iter = list.iterator(); iter.hasNext();) {
        if (iter.next() == null) {
          iter.remove();
        }
      }
    }
  }

}
