package org.nlogo.headless

import org.nlogo.api.JobOwner
import org.nlogo.nvm.{EngineException, Instruction, Context}

case class ErrorReport(owner: JobOwner, context: Context, instruction: Instruction, ex: Exception) {
  def getNetLogoStackTrace = {
    ex match {
      case ee: EngineException => Some(context.buildRuntimeErrorMessage(ee.instruction, ee))
      case _ => None
    }
  }
}
