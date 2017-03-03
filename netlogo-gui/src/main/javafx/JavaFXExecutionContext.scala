// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.javafx

import javafx.application.Platform
import scala.concurrent.ExecutionContext

object JavaFXExecutionContext extends ExecutionContext {
  def execute(runnable: Runnable): Unit =
    Platform.runLater(runnable)

  def reportFailure(cause: Throwable): Unit =
    ExecutionContext.defaultReporter(cause)
}
