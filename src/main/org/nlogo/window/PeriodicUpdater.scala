// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.event.{ ActionEvent, ActionListener }
import javax.swing.Timer
import org.nlogo.api.PeriodicUpdateDelay
import org.nlogo.nvm.JobManagerInterface

class PeriodicUpdater(jobManager: JobManagerInterface)
    extends Timer(PeriodicUpdateDelay.PERIODIC_UPDATE_DELAY, null) with ActionListener {
  addActionListener(this)
  def actionPerformed(e: ActionEvent): Unit = jobManager.timeToRunSecondaryJobs()
}
