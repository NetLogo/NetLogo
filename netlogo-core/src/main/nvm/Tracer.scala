// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

abstract class Tracer {

  // control
  def enable()
  def disable()
  def reset()
  def dump(stream: java.io.PrintStream)

  // call recording
  def openCallRecord(context: Context, activation: Activation)
  def closeCallRecord(context: Context, activation: Activation)
  def calls(name: String): Long
  def exclusiveTime(name: String): Long
  def inclusiveTime(name: String): Long

}
