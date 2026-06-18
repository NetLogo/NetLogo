// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.bsapp

trait ActiveView {
  def paintView(): Unit
  def disable(): Unit
}
