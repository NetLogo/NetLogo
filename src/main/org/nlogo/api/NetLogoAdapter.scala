// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/**
 * Implements NetLogoListener with empty methods, which can be overriden
 * in subclasses.
 */

trait NetLogoAdapter extends NetLogoListener {
  override def possibleViewUpdate { }
  override def tickCounterChanged(ticks: Double) { }
  override def switchChanged(name: String, value: Boolean, valueChanged: Boolean) { }
  override def sliderChanged(name: String, value: Double, min: Double, increment: Double, max: Double, valueChanged: Boolean, buttonReleased: Boolean) { }
  override def modelOpened(name: String) { }
  override def inputBoxChanged(name: String, value: AnyRef, valueChanged: Boolean) { }
  override def commandEntered(owner: String, text: String, agentKind: Char, errorMsg: CompilerException) { }
  override def codeTabCompiled(text: String, errorMsg: CompilerException) { }
  override def chooserChanged(name: String, value: AnyRef, valueChanged: Boolean) { }
  override def buttonStopped(buttonName: String) { }
  override def buttonPressed(buttonName: String) { }
}
