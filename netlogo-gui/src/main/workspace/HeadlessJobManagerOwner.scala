// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.api.{ Exceptions, JobOwner, LogoException }
import org.nlogo.nvm.JobManagerOwner

// The name "Headless" here simply indicates that this is not tied to the GUI.
// When user feedback via the GUI is desired, the GUIJobManagerOwner should be used.
class HeadlessJobManagerOwner(messageCenter: WorkspaceMessageCenter) extends JobManagerOwner {
  def runtimeError(owner: JobOwner,
    manager: org.nlogo.nvm.JobManagerInterface,
    context: org.nlogo.nvm.Context,
    instruction: org.nlogo.nvm.Instruction,
    ex: Exception) {
    ex match {
      case le: LogoException =>
        messageCenter.send(RuntimeError(owner, context, instruction, ex))
      case _ =>
        System.err.println("owner: " + owner.displayName)
        Exceptions.handle(ex)
    }
  }

  def ownerFinished(owner: JobOwner): Unit = { }
  def updateDisplay(haveWorldLockAlready: Boolean, forced: Boolean): Unit = { }
  def periodicUpdate(): Unit = { }
}
