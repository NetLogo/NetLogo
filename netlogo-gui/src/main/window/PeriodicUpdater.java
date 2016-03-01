// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.api.PeriodicUpdateDelay;
import org.nlogo.nvm.JobManagerInterface;

public strictfp class PeriodicUpdater
    extends javax.swing.Timer
    implements java.awt.event.ActionListener {

  private final JobManagerInterface jobManager;

  public PeriodicUpdater(JobManagerInterface jobManager) {
    super(PeriodicUpdateDelay.PERIODIC_UPDATE_DELAY(), null);
    this.jobManager = jobManager;
    addActionListener(this);
  }

  public void actionPerformed(java.awt.event.ActionEvent e) {
    jobManager.timeToRunSecondaryJobs();
  }

}
