// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.internalapi

import org.nlogo.core.CompilerException

trait Monitorable[A] {
  def defaultValue: A
  def currentValue: A

  // this callback will be called on the JavaFX UI thread when the value changes
  def onUpdate(callback: A => Unit): Unit

  def onError(callback: Exception => Unit): Unit
}
