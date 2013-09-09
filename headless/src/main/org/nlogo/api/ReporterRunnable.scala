// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import scala.language.implicitConversions

object ReporterRunnable {
  implicit def thunk2ReporterRunnable[T](fn: () => T): ReporterRunnable[T] =
    new ReporterRunnable[T] { def run = fn() }
}

trait ReporterRunnable[T] {
  def run(): T
}
