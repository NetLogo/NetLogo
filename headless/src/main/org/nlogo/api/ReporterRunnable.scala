// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait ReporterRunnable[T] {
  def run(): T
}
