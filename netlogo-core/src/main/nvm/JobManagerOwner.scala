// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.JobOwner

trait JobManagerOwner {
  // have the JobManager pass itself in here to make it easy to call halt
  def runtimeError(owner: JobOwner, manager: JobManagerInterface, context: Context, instruction: Instruction, ex: Exception)
  def ownerFinished(owner: JobOwner)
  def updateDisplay(haveWorldLockAlready: Boolean, forced: Boolean)
  def periodicUpdate()
}
