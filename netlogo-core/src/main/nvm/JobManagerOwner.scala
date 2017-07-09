// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.JobOwner

trait JobManagerOwner {
  def runtimeError(owner: JobOwner, context: Context, instruction: Instruction, ex: Exception)
  def ownerFinished(owner: JobOwner)
  def updateDisplay(haveWorldLockAlready: Boolean)
  def periodicUpdate()
}
