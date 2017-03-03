// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import scala.concurrent.ExecutionContext

object NetLogoExecutionContext {
  implicit def backgroundExecutionContext: ExecutionContext =
    ExecutionContext.global
}
