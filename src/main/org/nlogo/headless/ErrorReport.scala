// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.JobOwner
import org.nlogo.nvm.{ EngineException, Instruction, Context }

case class ErrorReport(owner: JobOwner, context: Context, instruction: Instruction, ex: Exception) {
  lazy val stackTrace =
    Some(ex).collect{
      case ee: EngineException =>
        context.buildRuntimeErrorMessage(ee.instruction, ee)
    }
}
