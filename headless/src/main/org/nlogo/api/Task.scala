// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

sealed trait Task
trait ReporterTask extends Task {
  def report(c: Context, args: Array[AnyRef]): AnyRef
}
trait CommandTask extends Task {
  def perform(c: Context, args: Array[AnyRef])
}

