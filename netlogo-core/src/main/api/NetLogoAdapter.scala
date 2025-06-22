// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.CompilerException

/**
 * Implements NetLogoListener with empty methods, which can be overridden
 * in subclasses.
 */

trait NetLogoAdapter extends NetLogoListener {
  override def possibleViewUpdate(): Unit = { }
  override def tickCounterChanged(ticks: Double): Unit = { }
  override def switchChanged(name: String, value: Boolean, valueChanged: Boolean): Unit = { }
  override def sliderChanged(name: String, value: Double, min: Double, increment: Double, max: Double, valueChanged: Boolean, buttonReleased: Boolean): Unit = { }
  override def modelOpened(name: String): Unit = { }
  override def inputBoxChanged(name: String, value: AnyRef, valueChanged: Boolean): Unit = { }
  override def commandEntered(owner: String, text: String, agentType: Char, errorMsg: CompilerException): Unit = { }
  override def codeTabCompiled(text: String, errorMsg: CompilerException): Unit = { }
  override def chooserChanged(name: String, value: AnyRef, valueChanged: Boolean): Unit = { }
  override def buttonStopped(buttonName: String): Unit = { }
  override def buttonPressed(buttonName: String): Unit = { }
}
