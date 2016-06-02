// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.Syntax

sealed trait Task {
  /**
   * Used to specify the number of arguments required to run a task.
   * Note that a task may be supplied with more arguments depending
   * on the number of arguments supplied to run/runresult.
   * While this is only used to calculate argument count at the moment,
   * future versions may check whether the task is variadic and may
   * make use of the type information to provide error messages.
   */
  def syntax: Syntax
}

trait ReporterTask extends Task {
  def report(c: Context, args: Array[AnyRef]): AnyRef
}

trait CommandTask extends Task {
  def perform(c: Context, args: Array[AnyRef])
}
