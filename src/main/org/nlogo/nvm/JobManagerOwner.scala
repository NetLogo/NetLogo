// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.JobOwner

trait JobManagerOwner {
  def runtimeError(owner: JobOwner, context: Context, instruction: Instruction, ex: Exception): Unit
  def ownerFinished(owner: JobOwner): Unit
  def updateDisplay(haveWorldLockAlready: Boolean): Unit
  def periodicUpdate(): Unit
}
