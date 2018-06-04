// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

trait ProgressListener {
  def start(): Unit = {}
  def progress(fraction: Double): Unit = {}
  def finish(): Unit = {}
}
