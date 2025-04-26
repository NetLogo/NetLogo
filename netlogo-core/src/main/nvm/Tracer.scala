// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import java.util.Set

abstract class Tracer {

  // control
  def enable(): Unit
  def disable(): Unit
  def reset(): Unit
  def dump(stream: java.io.PrintStream): Unit

  // call recording
  def openCallRecord(context: Context, activation: Activation): Unit
  def closeCallRecord(context: Context, activation: Activation): Unit
  def calls(name: String): Long
  def exclusiveTime(name: String): Long
  def inclusiveTime(name: String): Long

  def procedureNames(): Set[String]

}
