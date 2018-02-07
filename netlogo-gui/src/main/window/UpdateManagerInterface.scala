// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.UpdateMode

trait UpdateManagerInterface {
  def shouldUpdateNow: Boolean
  def shouldComeUpForAirAgain: Boolean
  def speed: Double
  def speed_=(d: Double): Unit
  def recompute(): Unit
  def frameRate: Double
  def frameRate(rate: Double): Unit
  def updateMode: UpdateMode
  def updateMode(rate: UpdateMode): Unit
  def pause(): Unit
  def reset(): Unit
  def pseudoTick(): Unit
  def beginPainting(): Unit
  def donePainting(): Unit
  def isDoneSmoothing(): Boolean
  def nudgeSleeper(): Unit
}
