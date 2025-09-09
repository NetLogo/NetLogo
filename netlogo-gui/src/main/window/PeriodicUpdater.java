// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

import org.nlogo.api.PeriodicUpdateDelay;
import org.nlogo.nvm.JobManagerInterface;

public class PeriodicUpdater extends Timer {
  private final JobManagerInterface jobManager;

  public PeriodicUpdater(JobManagerInterface jobManager) {
    super(PeriodicUpdateDelay.DelayInMilliseconds(), null);
    this.jobManager = jobManager;
  }

  public void createActionListener() {
    addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        jobManager.timeToRunSecondaryJobs();
      }
    });
  }
}
