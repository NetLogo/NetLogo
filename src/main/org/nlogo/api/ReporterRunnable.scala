// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait ReporterRunnable[T] {
  @throws(classOf[LogoException])
  def run(): T
}
